package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import ru.nobirds.torrent.client.message.HandshakeMessage
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.utils.Id
import java.nio.charset.Charset

public object HandshakeMessageSerializer : MessageSerializer<HandshakeMessage> {

    private val zeroBytes = Unpooled.wrappedBuffer(ByteArray(8))

    override fun read(length: Int, messageType: MessageType, stream: ByteBuf): HandshakeMessage {
        stream.readerIndex(stream.readerIndex()-1)

        val protocolLength = length - (8 + 20 + 20)

        val protocol = stream.readBytes(protocolLength)

        stream.skipBytes(8)

        val hash = stream.readBytes(20)
        val peer = stream.readBytes(20)

        val message = HandshakeMessage(
                Id.fromBuffer(hash),
                Id.fromBuffer(peer),
                protocol.toString(Charset.forName("UTF-8"))
        )

        // sequenceOf(protocol, hash, peer).forEach { it.release() }

        return message
    }

    override fun write(stream: ByteBuf, message: HandshakeMessage) {
        val protocolBytes = message.protocol.toByteArray("UTF-8")

        stream.writeInt(protocolBytes.size() + 8 + 20 + 20)
        stream.writeBytes(protocolBytes)
        stream.writeBytes(zeroBytes)
        stream.writeBytes(message.hash.toBytes())
        stream.writeBytes(message.peer.toBytes())
    }

}
