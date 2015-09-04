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
import java.net.InetSocketAddress

public class TorrentMessageCodec(val serializerProvider: MessageSerializerProvider) : ByteToMessageCodec<Message>(Message::class.java) {

    object Attributes {

        public val peer: AttributeKey<Peer> = AttributeKey.valueOf<Peer>("peer")
        public val handshake: AttributeKey<Handshake> = AttributeKey.valueOf<Handshake>("handshake")

    }

    override fun decode(ctx: ChannelHandlerContext, buffer: ByteBuf, out: MutableList<Any>) {
        val length = buffer.readInt()

        buffer.readerIndex(buffer.readerIndex()-4)

        if (length > buffer.readableBytes()-4) {
            return
        }

        val message = serializerProvider.unmarshall(buffer)

        val peer = getPeerOrHandshake(ctx, message, false)

        out.add(PeerAndMessage(peer, message))
    }

    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: ByteBuf) {
        checkHandshake(ctx, msg, true)

        serializerProvider.marshall(msg, out)
    }

    private fun getPeerOrHandshake(ctx: ChannelHandlerContext, message: Message, local: Boolean): Peer {
        checkHandshake(ctx, message, local)

        val peer = ctx.attr(Attributes.peer).get()

        requireNotNull(peer, "Need handshake before")

        return peer!!
    }

    private fun checkHandshake(ctx: ChannelHandlerContext, message: Message, local: Boolean) {
        if (message is HandshakeMessage) {
            val handshake = ctx.attr(Attributes.handshake).getOrSet { Handshake() }

            require(handshake.complete.not(), "Unexpected handshake: $message")

            handshake.check(local)

            if (!local) {
                ctx.attr(Attributes.peer)
                        .getOrSet { Peer(message.hash, message.peer, ctx.channel().remoteAddress() as InetSocketAddress) }
            }
        } else {
            require(ctx.attr(Attributes.handshake).get()?.complete ?: false)
        }
    }

}