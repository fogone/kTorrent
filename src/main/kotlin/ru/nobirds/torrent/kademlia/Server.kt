package ru.nobirds.torrent.kademlia

import java.net.DatagramSocket
import java.net.InetAddress
import java.net.DatagramPacket
import java.io.ByteArrayInputStream
import ru.nobirds.torrent.kademlia.message.Message

public class Server(val port:Int) : Thread("Kademlia Server") {

    private val localNode = Node(Ids.random(), InetAddress.getByName("localhost")!!)

//    private val outputMessages = ArrayBlockingQueue<Message>(50)
//    private val inputMessages = ArrayBlockingQueue<Message>(50)

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

    }

    public fun send(message: Message) {

    }
}