package ru.nobirds.torrent.client.task.connection

import ru.nobirds.torrent.client.message.MessageSerializerFactory
import ru.nobirds.torrent.client.message.Message
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.io.DataOutputStream
import java.io.OutputStream

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

