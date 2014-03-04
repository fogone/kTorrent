package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.MessageSerializerFactory
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
import ru.nobirds.torrent.client.message.BitFieldMessage

public class Connection(val task:TorrentTask, val socket:Socket) : Thread("Connection") {

    private val torrentState:TorrentState = TorrentState(task.torrent.info.hashes.size)

    private val input:InputStream = ConnectionInputStream(socket.getInputStream()!!)
    private val output:OutputStream = socket.getOutputStream()!!

    private val handlers = createMessageHandlers()

    private fun createMessageHandlers():Map<MessageType, MessageHandler<out Message>> = hashMapOf(
            MessageType.bitfield to BitFieldMessageHandler(torrentState)
    )

    fun sendMessage(message:Message) {
        val serializer = MessageSerializerFactory.getSerializer(message.messageType)
                            as MessageSerializer<Message>

        serializer.write(output, message)
    }

    private fun handleMessage(message:Message) {
        val handler = handlers.getOrElse(message.messageType) { DoNothingMessageHandler }
                            as MessageHandler<Message>

        handler.handle(message)
    }

    override fun run() {
        do {
            try {
                sendMessage(BitFieldMessage(task.state.state))
                readAndHandleMessage()
            } catch(e: Exception) {
                e.printStackTrace() // todo
                socket.closeQuietly()
                break;
            }
        } while(true)

        task.sendMessage(RemoveConnectionMessage(this))
    }

    private fun readAndHandleMessage() {
        val messageType = MessageSerializerFactory.findMessageTypeByValue(input.read())
        val serializer = MessageSerializerFactory.getSerializer(messageType)
        handleMessage(serializer.read(messageType, input))
    }

}

