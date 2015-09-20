package ru.nobirds.torrent.client.connection

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageCodec
import io.netty.util.AttributeKey
import ru.nobirds.torrent.client.message.HandshakeMessage
import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.serializer.MessageSerializerProvider
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.getOrSet
import ru.nobirds.torrent.utils.log
import ru.nobirds.torrent.utils.rewind
import java.net.InetSocketAddress

class ConnectionState() {

    var localHandshakeSent:Boolean = false
    var remoteHandshakeReceived:Boolean = false

    var peer:Peer? = null

    var isCurrentMessageHandshake = false

    public val handshakeComplete:Boolean
        get() = localHandshakeSent && remoteHandshakeReceived

}

object Attributes {

    public val connectionState: AttributeKey<ConnectionState> = AttributeKey.valueOf<ConnectionState>("state")


}

fun ChannelHandlerContext.getState():ConnectionState = attr(Attributes.connectionState).getOrSet { ConnectionState() }

public class TorrentMessageCodec(val serializerProvider: MessageSerializerProvider) : ByteToMessageCodec<Message>(Message::class.java) {

    private val logger = log()

    override fun decode(ctx: ChannelHandlerContext, buffer: ByteBuf, out: MutableList<Any>) {
        val message = extractFrame(ctx, buffer)
        if (message != null) {
            val msg = deserialize(ctx, message)

            logger.debug("Received message {} from {}", msg.messageType, ctx.channel().remoteAddress())

            out.add(msg)
        }
    }

    fun extractFrame(ctx: ChannelHandlerContext, buffer: ByteBuf):ByteBuf? {
        val state = ctx.getState()

        val isHandshake = !state.remoteHandshakeReceived

        val lengthFieldSize = if (isHandshake) 1 else 4

        if (buffer.readableBytes() < lengthFieldSize) {
            return null
        }

        val length = if(isHandshake) buffer.readUnsignedByte().toInt() + 8 + 20 + 20 else buffer.readUnsignedInt().toInt()

        if (buffer.readableBytes() < length) {
            buffer.rewind(-lengthFieldSize)
            return null
        }

        val frame = ctx.alloc().buffer(length)

        frame.writeBytes(buffer, buffer.readerIndex(), length)

        buffer.rewind(length)

        if (isHandshake) {
            state.remoteHandshakeReceived = true
            state.isCurrentMessageHandshake = true
        } else {
            state.isCurrentMessageHandshake = false
        }

        return frame
    }

    private fun deserialize(ctx: ChannelHandlerContext, buffer: ByteBuf): Message {
        val state = ctx.getState()

        return if (state.isCurrentMessageHandshake) {
            val handshakeMessage = serializerProvider.unmarshallHandshake(buffer)

            state.peer = Peer(handshakeMessage.hash, handshakeMessage.peer,
                    ctx.channel().remoteAddress() as InetSocketAddress)

            handshakeMessage.complete = state.handshakeComplete

            handshakeMessage
        } else {
            serializerProvider.unmarshall(buffer)
        }
    }

    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: ByteBuf) {
        if (msg is HandshakeMessage) {
            val state = ctx.getState()

            if(state.localHandshakeSent)
                throw IllegalArgumentException("Duplicate handshake")

            state.localHandshakeSent = true

            serializerProvider.marshallHandshake(out, msg)
        } else {
            serializerProvider.marshall(msg, out)
        }

        logger.debug("Sent message {} to {}", msg.messageType, ctx.channel().remoteAddress())
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        ctx.channel().close()
    }
}
