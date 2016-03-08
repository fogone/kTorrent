package ru.nobirds.torrent.client.connection

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption
import io.netty.channel.ChannelPipeline
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
import ru.nobirds.torrent.utils.log
import ru.nobirds.torrent.utils.queueHandlerThread
import java.io.Closeable
import java.net.SocketAddress
import java.util.concurrent.ArrayBlockingQueue

data class PeerAndMessage(val peer: Peer, val message: Any)

class ConnectMessage(val onConnect:()->Unit)

interface ConnectionManager : Closeable {

    fun connect(peer: Peer, onConnect:()->Unit) {
        send(peer, ConnectMessage(onConnect))
    }

    fun send(message: PeerAndMessage)

    fun send(peer: Peer, message: Any) {
        send(PeerAndMessage(peer, message))
    }

    fun read(): PeerAndMessage

}

class NettyConnectionManager(val port:Int) : ConnectionManager {

    private val logger = log()

    private val registry = ConnectionRegistry()
    private val incoming = ArrayBlockingQueue<PeerAndMessage>(1000)
    private val outgoing = ArrayBlockingQueue<PeerAndMessage>(1000)

    private val acceptGroup = NioEventLoopGroup()
    private val workerGroup = NioEventLoopGroup()
    private val clientGroup = NioEventLoopGroup()

    private val messageSerializerProvider = MessageSerializerProvider()

    private val server = ServerBootstrap()
            .group(acceptGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler<Channel> { it.pipeline().setupHandlers() }
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .bind(port).channel()

    private val clientBootstrap = Bootstrap()
            .group(clientGroup)
            .channel(NioSocketChannel::class.java)
            .channelInitializerHandler<Channel,Bootstrap>{ it.pipeline().setupHandlers() }
            .option(ChannelOption.SO_KEEPALIVE, true)

    private fun ChannelPipeline.setupHandlers() {
        this.addLast("TorrentCodec", TorrentMessageCodec(messageSerializerProvider))
        this.addLast("RequestQueueStorage", requestQueueStorage<PeerAndMessage>(incoming))
    }

    private val sendWorker = queueHandlerThread(outgoing) { sendMessage(it) }

    private fun sendMessage(message: PeerAndMessage) {
        val (peer, subMessage) = message

        when (subMessage) {
            is ConnectMessage -> connect(peer.address).addCompleteListener {
                if (it.isSuccess) {
                    if (!registry.registered(peer)) {
                        registry.register(peer, it.channel())
                        subMessage.onConnect()
                    }
                } else {
                    registry.unregister(peer)
                }
            }
            is Message -> {
                val channel = registry.find(peer)

                if (channel != null) {
                    logger.debug("Sending message {}.", subMessage.messageType)

                    channel.writeAndFlush(subMessage)
                }
                else logger.warn("Message for peer without connection {}. Ignored.", message)
            }
            else -> throw IllegalArgumentException("Unsupported message $message}")
        }
    }

    override fun send(message: PeerAndMessage) {
        logger.debug("ConnectionManager accept message {} from {} to send",
                message.message.javaClass.simpleName, message.peer.address)

        outgoing.put(message)
    }

    override fun read(): PeerAndMessage = incoming.take()

    private fun connect(address: SocketAddress): ChannelFuture {
        return clientBootstrap.connect(address)
    }

    override fun close() {
        sendWorker.interrupt()
        acceptGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
        clientGroup.shutdownGracefully()
        server.closeFuture().sync()
    }
}
