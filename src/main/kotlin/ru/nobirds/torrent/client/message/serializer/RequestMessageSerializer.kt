package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.client.message.RequestMessage

public object RequestMessageSerializer : MessageSerializer<RequestMessage> {

    override fun read(length: Int, messageType: MessageType, stream: ByteBuf): RequestMessage {
        val index = stream.readInt()
        val begin = stream.readInt()
        val blockLength = stream.readInt()
        return RequestMessage(index, begin, blockLength)
    }

    override fun write(stream: ByteBuf, message: RequestMessage) {
        stream.writeInt(13)
        stream.writeByte(message.messageType.value)
        stream.writeInt(message.index)
        stream.writeInt(message.begin)
        stream.writeInt(message.length)
    }

}