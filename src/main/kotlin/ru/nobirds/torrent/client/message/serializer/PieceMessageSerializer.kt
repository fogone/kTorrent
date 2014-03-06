package ru.nobirds.torrent.client.message

import java.io.DataOutputStream
import java.util.BitSet
import java.io.DataInputStream

public object PieceMessageSerializer : MessageSerializer<PieceMessage> {

    override fun read(length: Int, messageType: MessageType, stream: DataInputStream): PieceMessage {
        val index = stream.readInt()
        val begin = stream.readInt()
        val buffer = ByteArray(length - 8)
        stream.readFully(buffer)
        return PieceMessage(index, begin, buffer)
    }

    override fun write(stream: DataOutputStream, message: PieceMessage) {
        stream.writeInt(message.block.size + 9)
        stream.writeByte(message.messageType.value)
        stream.writeInt(message.index)
        stream.writeInt(message.begin)
        stream.write(message.block)
    }

}