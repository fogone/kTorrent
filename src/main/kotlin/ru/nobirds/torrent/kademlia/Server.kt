package ru.nobirds.torrent.kademlia

import java.net.DatagramSocket
import java.net.DatagramPacket
import java.io.ByteArrayInputStream
import ru.nobirds.torrent.kademlia.message.Message
import ru.nobirds.torrent.kademlia.message.MessageSerializer
import java.util.ArrayList
import java.io.ByteArrayOutputStream
import java.util.concurrent.ArrayBlockingQueue
import java.net.InetSocketAddress

public class Server(
        val port:Int,
        val messageSerializer:MessageSerializer) : Thread("Kademlia Server") {

    private val sendListeners = ArrayList<(Message)->Unit>()

    private val receiveListeners = ArrayList<(Message)->Unit>()

    private val outputMessagesQueue = ArrayBlockingQueue<Message>(50)

    private val socket = DatagramSocket(port)

    override fun run() {
        runSendingThread()

        val packet = DatagramPacket(ByteArray(1024), 1024)

        while(isInterrupted().not()) {
            try {
                socket.receive(packet)
                processPacket(packet)
            } catch(e: Exception) {
                e.printStackTrace() // todo
            }
        }
    }

    private fun runSendingThread() {
        run {
            while(!isInterrupted())
                sendMessage(outputMessagesQueue.take()!!)
        }
    }

    private fun sendMessage(message:Message) {
        try {
            val result = ByteArrayOutputStream()

            sendListeners.forEach { it(message) }

            messageSerializer.serialize(message, result)

            val bytes = result.toByteArray()

            socket.send(DatagramPacket(bytes, bytes.size))
        } catch(e: Exception) {
            e.printStackTrace() // todo
        }
    }

    private fun processPacket(packet:DatagramPacket) {
        val data = packet.getData()!!
        val offset = packet.getOffset()
        val length = packet.getLength()
        val address = packet.getSocketAddress() as InetSocketAddress

        val inputStream = ByteArrayInputStream(data, offset, length)

        val message = messageSerializer.deserialize(address, inputStream)

        receiveListeners.forEach { it(message) }
    }

    public fun send(message: Message) {
        outputMessagesQueue.put(message)
    }

    public fun registerSendListener(listener:(Message)->Unit):Server {
        sendListeners.add(listener)
        return this
    }

    public fun registerReceiveListener(listener:(Message)->Unit):Server {
        receiveListeners.add(listener)
        return this
    }

}