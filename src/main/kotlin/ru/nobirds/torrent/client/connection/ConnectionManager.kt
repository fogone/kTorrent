package ru.nobirds.torrent.client.connection

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.ByteToMessageCodec
import io.netty.util.AttributeKey
import ru.nobirds.torrent.bencode.requestQueueStorage
import ru.nobirds.torrent.client.message.HandshakeMessage
import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.serializer.MessageSerializerProvider
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.*
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap

public data class PeerAndMessage(val peer: Peer, val message: Message)
public data class AddressAndMessage(val address: InetSocketAddress, val message: Message)

public data class Handshake(var local:Boolean = false, var remote:Boolean = false) {

    public val complete:Boolean
        get() = local && remote

    public fun check(local: Boolean) {
        if(local) this.local = true
        else this.remote = true
    }

}

class ConnectionRegistry {

    private val connections = ConcurrentHashMap<InetSocketAddress, ChannelHandlerContext>()

    fun register(peer: InetSocketAddress, context: ChannelHandlerContext) {
        connections.put(peer, context)
    }

    fun unregister(peer: InetSocketAddress) {
        connections.remove(peer)
    }

    fun find(peer: InetSocketAddress):ChannelHandlerContext? {
        val context = connections.get(peer)

        if(context == null || context.isRemoved) {
            unregister(peer)
            return null
        }

        return context
    }

}

public object Attributes {

    public val peer:AttributeKey<Peer> = AttributeKey.valueOf<Peer>("peer")
    public val handshake:AttributeKey<Handshake> = AttributeKey.valueOf<Handshake>("handshake")

}

public class TorrentMessageCodec(val serializerProvider: MessageSerializerProvider, val registry: ConnectionRegistry) : ByteToMessageCodec<Message>() {

    override fun disconnect(ctx: ChannelHandlerContext, promise: ChannelPromise) {
        registry.unregister(ctx.channel().remoteAddress() as InetSocketAddress)

        ctx.disconnect(promise)
    }

    override fun connect(ctx: ChannelHandlerContext, remoteAddress: SocketAddress, localAddress: SocketAddress, promise: ChannelPromise) {
        ctx.connect(remoteAddress)

        registry.register(remoteAddress as InetSocketAddress, ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    override fun decode(ctx: ChannelHandlerContext, buffer: ByteBuf, out: MutableList<Any>) {
        val length = buffer.readInt()

        if (length < buffer.readableBytes()) {
            buffer.readerIndex(buffer.readerIndex()-1)
            return
        }

        val type = buffer.readByte().toInt()

        val serializer = serializerProvider.getSerializerByType<Message>(type)
        val messageType = serializerProvider.findMessageTypeByValue(type)

        val message = serializer.read(length, messageType, buffer)

        handshake(ctx, message, false)

        val peer = ctx.attr(Attributes.peer).get()

        requireNotNull(peer, "Need handshake before")

        out.add(PeerAndMessage(peer, message))
    }

    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: ByteBuf) {
        handshake(ctx, msg, true)

        serializerProvider.getSerializer<Message>(msg.messageType).write(out, msg)
    }

    private fun handshake(ctx: ChannelHandlerContext, message: Message, local: Boolean) {
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

public interface ConnectionManager : Closeable {

    fun send(message: AddressAndMessage)

    fun send(address: InetSocketAddress, message: Message) {
        send(AddressAndMessage(address, message))
    }

    fun read(): PeerAndMessage

}

public class NettyConnectionManager(val port:Int) : ConnectionManager {

    private val registry = ConnectionRegistry()
    private val incoming = ArrayBlockingQueue<PeerAndMessage>(1000)
    private val outgoing = ArrayBlockingQueue<AddressAndMessage>(1000)

    private val acceptGroup = NioEventLoopGroup()
    private val workerGroup = NioEventLoopGroup()
    private val clientGroup = NioEventLoopGroup()

    private val server = ServerBootstrap()
            .group(acceptGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler<Channel> {
                it.pipeline()
                        .addLast(TorrentMessageCodec(MessageSerializerProvider(), registry))
                        .addLast(requestQueueStorage(incoming))
            }
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .bind(port)

    private val clientBootstrap = Bootstrap()
            .group(clientGroup)
            .channel(NioSocketChannel::class.java)
            .channelInitializerHandler<Channel, Bootstrap> {
                it.pipeline()
                        .addLast(TorrentMessageCodec(MessageSerializerProvider(), registry))
                        .addLast(requestQueueStorage(incoming))
            }
            .option(ChannelOption.SO_KEEPALIVE, true)

    private val sendWorker = queueHandlerThread(outgoing) { message ->
        var context = registry.find(message.address)

        if (context == null) {
            connect(message.address).addCompleteListener {
                if (it.isSuccess) {
                    outgoing.put(message)
                }
            }
        } else {
            val ctx = context
            synchronized(ctx) {
                ctx.writeAndFlush(message.message)
            }
        }
    }

    public override fun send(message: AddressAndMessage) {
        outgoing.put(message)
    }

    public override fun read(): PeerAndMessage = incoming.take()

    private fun connect(address: SocketAddress): ChannelFuture {
        return clientBootstrap.remoteAddress(address).connect()
    }

    override fun close() {
        sendWorker.interrupt()
        acceptGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
        clientGroup.shutdownGracefully()
        server.channel().closeFuture().sync()
    }
}
