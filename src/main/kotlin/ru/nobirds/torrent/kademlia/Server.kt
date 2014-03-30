package ru.nobirds.torrent.kademlia

import java.net.DatagramSocket
import java.net.InetAddress
import java.net.DatagramPacket
import java.io.ByteArrayInputStream
import ru.nobirds.torrent.kademlia.message.Message
import ru.nobirds.torrent.kademlia.message.DefaultRequestContainer
import ru.nobirds.torrent.kademlia.message.ResponseMessage
import ru.nobirds.torrent.kademlia.message.RequestMessage
import ru.nobirds.torrent.kademlia.message.MessageSerializer
import ru.nobirds.torrent.kademlia.message.BencodeMessageSerializer
import java.util.ArrayList
import ru.nobirds.torrent.kademlia.message.MessageType
import java.io.ByteArrayOutputStream
import java.util.concurrent.ArrayBlockingQueue
import ru.nobirds.torrent.kademlia.message.ErrorMessage
import ru.nobirds.torrent.kademlia.message.bencode.BencodeMessageSerializer
import ru.nobirds.torrent.kademlia.message.MessageFactory

public class Server(val port:Int) : Thread("Kademlia Server") {

    private val requestContainer = DefaultRequestContainer()

    private val messageSerializer:MessageSerializer = BencodeMessageSerializer(requestContainer)

    private val listeners = ArrayList<(Message)->Unit>()

    private val socket = DatagramSocket(port)

    override fun run() {
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

    private fun processPacket(packet:DatagramPacket) {
        val data = packet.getData()!!
        val offset = packet.getOffset()
        val length = packet.getLength()

        val inputStream = ByteArrayInputStream(data, offset, length)

        val message = messageSerializer.deserialize(inputStream)

        listeners.forEach { it(message) }
    }

    public fun send(message: Message) {
        val result = ByteArrayOutputStream()

        if(message is RequestMessage)
            requestContainer.storeWithTimeout(message)

        messageSerializer.serialize(message, result)

        val bytes = result.toByteArray()

        socket.send(DatagramPacket(bytes, bytes.size))
    }

    public fun registerListener(listener:(Message)->Unit) {
        listeners.add(listener)
    }

}