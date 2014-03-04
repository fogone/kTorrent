package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.MessageFactory
import ru.nobirds.torrent.client.Peer
import kotlin.properties.Delegates
import java.net.Socket
import java.io.InputStream
import java.io.OutputStream
import java.io.FilterInputStream
import ru.nobirds.torrent.client.message.MessageSerializer
import ru.nobirds.torrent.client.message.BitFieldMessageHandler
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.client.message.MessageHandler
import ru.nobirds.torrent.client.message.DoNothingMessageHandler
import ru.nobirds.torrent.closeQuietly

public class Connection(val task:TorrentTask, val peer:Peer) : Thread("Connection") {

    private val socket by Delegates.lazy { Socket(peer.ip, peer.port) }

    private val input:InputStream by Delegates.lazy { ConnectionInputStream(socket.getInputStream()!!) }
    private val output:OutputStream by Delegates.lazy { socket.getOutputStream()!! }

    private val handlers = createMessageHandlers()

    private fun createMessageHandlers():Map<MessageType, MessageHandler<out Message>> = hashMapOf(
            MessageType.bitfield to BitFieldMessageHandler()
    )

    fun sendMessage(message:Message) {
        val serializer = MessageFactory.getSerializer(message.messageType) as MessageSerializer<Message>
        serializer.write(output, message)
    }

    override fun run() {
        do {
            try {
                readAndHandleMessage()
            } catch(e: Exception) {
                e.printStackTrace() // todo
                socket.closeQuietly()
                break;
            }
        } while(true)

        task.removeConnection(this)
    }

    private fun readAndHandleMessage() {
        val messageType = MessageFactory.findMessageTypeByValue(input.read())
        val serializer = MessageFactory.getSerializer(messageType)
        handleMessage(serializer.read(messageType, input))
    }

    private fun handleMessage(message:Message) {
        val handler = handlers.getOrElse(message.messageType) { DoNothingMessageHandler }
                        as MessageHandler<Message>

        handler.handle(message)
    }
}

