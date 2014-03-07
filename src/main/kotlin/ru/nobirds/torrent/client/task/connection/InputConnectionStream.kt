package ru.nobirds.torrent.client.task.connection

import ru.nobirds.torrent.client.message.MessageSerializerFactory
import ru.nobirds.torrent.client.message.Message
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.io.DataInputStream
import java.io.InputStream

class InputConnectionStream(val stream:InputStream) {

    private val input = DataInputStream(stream)

    public fun receive():Message {
        val length = input.readInt()
        val messageType = MessageSerializerFactory.findMessageTypeByValue(stream.read())

        return MessageSerializerFactory
                .getSerializer<Message>(messageType)
                .read(length - 1, messageType, input)
    }

}
