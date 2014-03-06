package ru.nobirds.torrent.client.message

import java.io.DataInputStream

abstract class AbstractMessageSerializer<T:Message>() : MessageSerializer<T> {

    private val emptyBuffer = ByteArray(0)

    override fun read(length: Int, messageType: MessageType, stream: DataInputStream): T {
        if(length > 0) {
            val buffer = ByteArray(length)
            stream.readFully(buffer)
            return deserialize(messageType, buffer)
        } else {
            return deserialize(messageType, emptyBuffer)
        }
    }

    protected abstract fun deserialize(messageType:MessageType, body:ByteArray): T

}
