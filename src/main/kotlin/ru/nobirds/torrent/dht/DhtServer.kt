package ru.nobirds.torrent.dht

import java.net.DatagramSocket
import java.net.DatagramPacket
import java.io.ByteArrayInputStream
import ru.nobirds.torrent.dht.message.Message
import ru.nobirds.torrent.dht.message.MessageSerializer
import java.util.ArrayList
import java.io.ByteArrayOutputStream
import java.util.concurrent.ArrayBlockingQueue
import java.net.InetSocketAddress
import kotlin.concurrent.thread
import ru.nobirds.torrent.peers.Peer
import kotlin.properties.Delegates
import ru.nobirds.torrent.bencode.BencodeParseException

public class DhtServer(val port:Int, val messageSerializer:MessageSerializer) {

    private var sendingThread: Thread by Delegates.notNull()

    private var receivingThread: Thread by Delegates.notNull()

    private val sendListeners = ArrayList<(AddressAndMessage)->Unit>()

    private val receiveListeners = ArrayList<(Message)->Unit>()

    private val sendMessagesQueue = ArrayBlockingQueue<AddressAndMessage>(500)
    private val receiveMessagesQueue = ArrayBlockingQueue<Message>(500)

    private val socket = DatagramSocket(port)

    public fun start() {
        runSendingThread()
        runReceivingThread()
    }

    fun join() {
        receivingThread.join()
        sendingThread.join()
    }

    private fun runReceivingThread() {
        this.receivingThread = thread(name = "dht server sending thread", start = true) {

            val packet = DatagramPacket(ByteArray(1024), 1024)

            while (true) {
                socket.receive(packet)
                processPacket(packet)
            }
        }
    }

    private fun runSendingThread() {
        this.sendingThread = thread(name = "dht server sending thread", start = true) {
            while (true) {
                sendMessage(sendMessagesQueue.take())
            }
        }
    }

    private fun sendMessage(addressAndMessage: AddressAndMessage) {
        try {
            val result = ByteArrayOutputStream()

            sendListeners.forEach { it(addressAndMessage) }

            messageSerializer.serialize(addressAndMessage.message, result)

            val bytes = result.toByteArray()

            socket.send(DatagramPacket(bytes, bytes.size, addressAndMessage.address))
        } catch(e: Exception) {
            e.printStackTrace() // todo
        }
    }

    private fun processPacket(packet:DatagramPacket) {
        try {
            val data = packet.getData()
            val offset = packet.getOffset()
            val length = packet.getLength()
            val address = packet.getSocketAddress() as InetSocketAddress

            val inputStream = ByteArrayInputStream(data, offset, length)

            val message = messageSerializer.deserialize(address, inputStream)

            receiveListeners.forEach { it(message) }

            receiveMessagesQueue.put(message)
        } catch(e: BencodeParseException) {
            println(e.getMessage()) // todo
        } catch(e: Exception) {
            e.printStackTrace() // todo
        }
    }

    public fun sendTo(addresses: Iterable<InetSocketAddress>, message: ()->Message) {
        for (address in addresses) {
            sendMessagesQueue.put(AddressAndMessage(address, message()))
        }
    }

    public fun sendTo(vararg addresses: InetSocketAddress, message: ()->Message) {
        sendTo(addresses.toList(), message)
    }

    public fun send(peers: Iterable<Peer>, message: ()->Message) {
        sendTo(peers.map { it.address }, message)
    }

    public fun receive():Message = receiveMessagesQueue.take()

    public fun registerSendListener(listener:(AddressAndMessage)->Unit): DhtServer {
        sendListeners.add(listener)
        return this
    }

    public fun registerReceiveListener(listener:(Message)->Unit): DhtServer {
        receiveListeners.add(listener)
        return this
    }

}