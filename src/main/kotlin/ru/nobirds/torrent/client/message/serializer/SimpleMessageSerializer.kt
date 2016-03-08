package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.client.message.SimpleMessage

object SimpleMessageSerializer : MessageSerializer<SimpleMessage> {

    override fun read(length: Int, messageType: MessageType, stream: ByteBuf): SimpleMessage
            = SimpleMessage(messageType)

    override fun write(stream: ByteBuf, message: SimpleMessage) {
        stream.writeInt(1)
        stream.writeByte(message.messageType.value)
    }

}
