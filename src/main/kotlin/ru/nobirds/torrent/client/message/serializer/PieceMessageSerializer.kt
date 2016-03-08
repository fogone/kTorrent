package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.client.message.PieceMessage
import ru.nobirds.torrent.client.task.state.Blocks

object PieceMessageSerializer : MessageSerializer<PieceMessage> {

    override fun read(length: Int, messageType: MessageType, stream: ByteBuf): PieceMessage {
        val index = stream.readInt()
        val begin = stream.readInt()
        val buffer = ByteArray(length - 8)
        stream.readBytes(buffer)
        return PieceMessage(Blocks.positionAndBytes(index, begin, buffer))
    }

    override fun write(stream: ByteBuf, message: PieceMessage) {
        stream.writeInt(message.positionAndBytes.block.size + 9)
        stream.writeByte(message.messageType.value)
        stream.writeInt(message.positionAndBytes.position.piece)
        stream.writeInt(message.positionAndBytes.position.begin)
        stream.writeBytes(message.positionAndBytes.block)
    }

}