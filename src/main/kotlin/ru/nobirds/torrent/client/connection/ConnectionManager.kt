package ru.nobirds.torrent.client.connection

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.ByteToMessageCodec
import ru.nobirds.torrent.bencode.BencodeCodec
import ru.nobirds.torrent.bencode.requestQueueStorage
import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.serializer.MessageSerializerProvider
import ru.nobirds.torrent.utils.addCompleteListener
import ru.nobirds.torrent.utils.channelInitializerHandler
import ru.nobirds.torrent.utils.childHandler
import ru.nobirds.torrent.utils.infiniteLoopThread
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap

public data class AddressAndMessage(val address: InetSocketAddress, val message: Message)

class ConnectionRegistry {

    private val connections = ConcurrentHashMap<SocketAddress, ChannelHandlerContext>()

    fun register(address: SocketAddress, context: ChannelHandlerContext) {
        connections.put(address, context)
    }

    fun unregister(address: SocketAddress) {
        connections.remove(address)
    }

    fun find(address: SocketAddress):ChannelHandlerContext? {
        val context = connections.get(address)

        if(context == null || context.isRemoved) {
            unregister(address)
            return null
        }

        return context
    }

}

class ConnectionRegistryChannelHandler(val registry: ConnectionRegistry) : ChannelHandlerAdapter() {

    override fun connect(ctx: ChannelHandlerContext, remoteAddress: SocketAddress,
                         localAddress: SocketAddress?, promise: ChannelPromise) {
        ctx.connect(remoteAddress, localAddress, promise)
        registry.register(remoteAddress, ctx)
    }

    override fun disconnect(ctx: ChannelHandlerContext, promise: ChannelPromise) {
        ctx.disconnect(promise)
        registry.unregister(ctx.channel().remoteAddress())
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}

public class TorrentMessageCodec(val serializerProvider: MessageSerializerProvider) : ByteToMessageCodec<Message>() {

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

        out.add(AddressAndMessage(ctx.channel().remoteAddress() as InetSocketAddress, message))
    }

    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: ByteBuf) {
        val serializer = serializerProvider.getSerializer<Message>(msg.messageType)
        serializer.write(out, msg)
    }

}

public interface ConnectionManager : Closeable {

    fun send(message: AddressAndMessage)

    fun send(address: InetSocketAddress, message: Message) {
        send(AddressAndMessage(address, message))
    }

    fun read(): AddressAndMessage

}

public class NettyConnectionManager(val port:Int) : ConnectionManager {

    private val registry = ConnectionRegistry()
    private val incoming = ArrayBlockingQueue<AddressAndMessage>(1000)
    private val outgoing = ArrayBlockingQueue<AddressAndMessage>(1000)

    private val acceptGroup = NioEventLoopGroup()
    private val workerGroup = NioEventLoopGroup()
    private val clientGroup = NioEventLoopGroup()

    private val server = ServerBootstrap()
            .group(acceptGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler<Channel> {
                it.pipeline()
                        .addLast(BencodeCodec())
                        .addLast(ConnectionRegistryChannelHandler(registry))
                        .addLast(requestQueueStorage(incoming))
            }
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .bind(port)

    private val clientBootstrap = Bootstrap()
            .group(clientGroup)
            .channel(NioSocketChannel::class.java)
            .channelInitializerHandler<Channel, Bootstrap> {
                it.pipeline()
                        .addLast(TorrentMessageCodec(MessageSerializerProvider()))
                        .addLast(ConnectionRegistryChannelHandler(registry))
                        .addLast(requestQueueStorage(incoming))
            }
            .option(ChannelOption.SO_KEEPALIVE, true)

    private val sendWorker = infiniteLoopThread {
        val message = outgoing.take()

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
                ctx.writeAndFlush(message)
            }
        }
    }

    public override fun send(message: AddressAndMessage) {
        outgoing.put(message)
    }

    public override fun read(): AddressAndMessage = incoming.take()

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
