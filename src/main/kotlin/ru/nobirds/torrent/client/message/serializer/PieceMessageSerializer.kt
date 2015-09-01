package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.client.message.PieceMessage

public object PieceMessageSerializer : MessageSerializer<PieceMessage> {

    override fun read(length: Int, messageType: MessageType, stream: ByteBuf): PieceMessage {
        val index = stream.readInt()
        val begin = stream.readInt()
        val buffer = ByteArray(length - 8)
        stream.readBytes(buffer)
        return PieceMessage(index, begin, buffer)
    }

    override fun write(stream: ByteBuf, message: PieceMessage) {
        stream.writeInt(message.block.size() + 9)
        stream.writeByte(message.messageType.value)
        stream.writeInt(message.index)
        stream.writeInt(message.begin)
        stream.writeBytes(message.block)
    }

}