package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.MessageSerializerFactory
import java.net.Socket
import java.io.InputStream
import java.io.OutputStream
import ru.nobirds.torrent.client.message.MessageSerializer
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.closeQuietly
import ru.nobirds.torrent.client.message.BitFieldMessage
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ArrayBlockingQueue
import java.io.DataInputStream
import java.io.DataOutputStream

class InputConnectionStreamThread(val stream:InputStream) : Thread("Input connection stream") {

    private val input = DataInputStream(stream)

    private val queue:BlockingQueue<Message> = ArrayBlockingQueue(300)

    public fun receive():Message = queue.take()!!

    override fun run() {
        while(!isInterrupted()) {
            queue.put(readMessage())
        }
    }

    private fun readMessage():Message {
        val length = input.readInt()
        val messageType = MessageSerializerFactory.findMessageTypeByValue(stream.read())

        return MessageSerializerFactory
                .getSerializer<Message>(messageType)
                .read(length - 1, messageType, input)
    }

}

class OutputConnectionStreamThread(val stream:OutputStream) : Thread("Output connection stream") {

    private val output = DataOutputStream(stream)

    private val queue:BlockingQueue<Message> = ArrayBlockingQueue(300)

    public fun send(message:Message) {
        queue.put(message)
    }

    override fun run() {
        while(!isInterrupted()) {
            writeMessage(queue.take()!!)
        }
    }

    private fun writeMessage(message:Message) {
        MessageSerializerFactory.getSerializer<Message>(message.messageType).write(output, message)
    }

}

public class Connection(val task:TorrentTask, val socket:Socket) : Thread("Connection handler thread") {

    private val torrentState:TorrentState = TorrentState(task.torrent.info)

    private val input = InputConnectionStreamThread(ConnectionInputStream(socket.getInputStream()!!))
    private val output = OutputConnectionStreamThread(socket.getOutputStream()!!)

    fun sendMessage(message:Message) {
        output.send(message)
    }

    override fun run() {
        while(!isInterrupted()) {

        }
    }
}

