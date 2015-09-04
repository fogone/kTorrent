package ru.nobirds.torrent.client.connection

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import ru.nobirds.torrent.bencode.requestQueueStorage
import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.serializer.MessageSerializerProvider
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.addCompleteListener
import ru.nobirds.torrent.utils.channelInitializerHandler
import ru.nobirds.torrent.utils.childHandler
import ru.nobirds.torrent.utils.queueHandlerThread
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ArrayBlockingQueue

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

    private val messageSerializerProvider = MessageSerializerProvider()

    private val server = ServerBootstrap()
            .group(acceptGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler<Channel> {
                it.pipeline()
                        .addLast(
                                ConnectionStorageHandler(registry),
                                TorrentMessageCodec(messageSerializerProvider),
                                requestQueueStorage(incoming)
                        )
            }
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .bind(port)

    private val clientBootstrap = Bootstrap()
            .group(clientGroup)
            .channel(NioSocketChannel::class.java)
            .channelInitializerHandler<Channel,Bootstrap>{
                it.pipeline()
                    .addLast(
                            ConnectionStorageHandler(registry),
                            TorrentMessageCodec(messageSerializerProvider),
                            requestQueueStorage(incoming)
                    )
            }
            .option(ChannelOption.SO_KEEPALIVE, true)


    private val sendWorker = queueHandlerThread(outgoing) { sendMessage(it) }

    private fun sendMessage(message: AddressAndMessage) {
        var channel = registry.find(message.address)

        if (channel == null) {
            connect(message.address).addCompleteListener {
                if(it.isSuccess) send(message)
                else {
                    if (it.channel().remoteAddress() != null)
                        registry.unregister(it.channel().remoteAddress())
                    it.get()
                }
            }
        } else {
            val chnl = channel
            synchronized(chnl) {
                chnl.writeAndFlush(message.message).sync()
            }
        }
    }

    public override fun send(message: AddressAndMessage) {
        outgoing.put(message)
    }

    public override fun read(): PeerAndMessage = incoming.take()

    private fun connect(address: SocketAddress): ChannelFuture {
        return clientBootstrap.connect(address)
    }

    override fun close() {
        sendWorker.interrupt()
        acceptGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
        clientGroup.shutdownGracefully()
        server.channel().closeFuture().sync()
    }
}
