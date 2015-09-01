package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.client.message.SimpleMessage

public object HandshakeMessageSerializer : MessageSerializer<SimpleMessage> {

    override fun read(length: Int, messageType: MessageType, stream: ByteBuf): SimpleMessage {
        stream.skipBytes(length)
        return SimpleMessage(messageType)
    }

    override fun write(stream: ByteBuf, message: SimpleMessage) {
        stream.writeInt(19)
        stream.writeBytes("BitTorrent protocol".toByteArray("UTF-8"))
    }

}
