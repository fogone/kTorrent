package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.client.message.PortMessage

object PortMessageSerializer : MessageSerializer<PortMessage> {

    override fun read(length: Int, messageType: MessageType, stream: ByteBuf): PortMessage
            = PortMessage(stream.readShort().toInt() and 0xffff)

    override fun write(stream: ByteBuf, message: PortMessage) {
        stream.writeInt(5)
        stream.writeByte(message.messageType.value)
        stream.writeShort(message.port)
    }

}