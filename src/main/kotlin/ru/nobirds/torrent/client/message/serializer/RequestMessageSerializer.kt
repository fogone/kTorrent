package ru.nobirds.torrent.client.message

import java.io.DataOutputStream
import java.util.BitSet
import java.io.DataInputStream

public object RequestMessageSerializer : MessageSerializer<RequestMessage> {

    override fun read(length: Int, messageType: MessageType, stream: DataInputStream): RequestMessage {
        val index = stream.readInt()
        val begin = stream.readInt()
        val blockLength = stream.readInt()
        return RequestMessage(index, begin, blockLength)
    }

    override fun write(stream: DataOutputStream, message: RequestMessage) {
        stream.writeInt(13)
        stream.writeByte(message.messageType.value)
        stream.writeInt(message.index)
        stream.writeInt(message.begin)
        stream.writeInt(message.length)
    }

}