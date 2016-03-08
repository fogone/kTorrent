package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.HaveMessage
import ru.nobirds.torrent.client.message.MessageType

object HaveMessageSerializer : MessageSerializer<HaveMessage> {

    override fun read(length: Int, messageType: MessageType, stream: ByteBuf): HaveMessage
            = HaveMessage(stream.readInt())

    override fun write(stream: ByteBuf, message: HaveMessage) {
        stream.writeInt(5)
        stream.writeByte(message.messageType.value)
        stream.writeInt(message.piece)
    }

}