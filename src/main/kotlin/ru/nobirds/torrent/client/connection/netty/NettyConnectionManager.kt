package ru.nobirds.torrent.client.connection.netty

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption
import io.netty.channel.ChannelPipeline
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.concurrent.DefaultEventExecutorGroup
import ru.nobirds.torrent.bencode.requestQueueStorage
import ru.nobirds.torrent.client.connection.ConnectionManager
import ru.nobirds.torrent.client.connection.PeerAndMessage
import ru.nobirds.torrent.client.message.serializer.MessageSerializerProvider
import ru.nobirds.torrent.utils.channelInitializerHandler
import ru.nobirds.torrent.utils.childHandler
import ru.nobirds.torrent.utils.log
import ru.nobirds.torrent.utils.queueHandlerThread
import java.net.InetSocketAddress
import java.util.concurrent.ArrayBlockingQueue

class NettyConnectionManager(val port: Int) : ConnectionManager {

    private val logger = log()

    private val registry = ConnectionRegistry()
    private val incoming = ArrayBlockingQueue<PeerAndMessage>(1000)
    private val outgoing = ArrayBlockingQueue<PeerAndMessage>(1000)

    private val codecEventExecutorGroup = DefaultEventExecutorGroup(4)
    private val queueStorageEventExecutorGroup = DefaultEventExecutorGroup(1)

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
            .channelInitializerHandler<Channel, Bootstrap> { it.pipeline().setupHandlers() }
            .option(ChannelOption.SO_KEEPALIVE, true)

    private fun ChannelPipeline.setupHandlers() {
        this.addFirst("ConnectionRegistry", ConnectionRegistryHandler(registry))
        this.addLast(codecEventExecutorGroup, "TorrentCodec", TorrentMessageCodec(messageSerializerProvider))
        this.addLast(queueStorageEventExecutorGroup, "RequestQueueStorage", requestQueueStorage<PeerAndMessage>(incoming))
    }

    private val sendWorker = queueHandlerThread(outgoing) { sendMessage(it) }

    private fun sendMessage(message: PeerAndMessage) {
        val (peer, subMessage) = message

        val channel = registry.find(peer.address)

        if (channel != null) {
            logger.debug("Sending message {}.", subMessage.messageType)

            channel.writeAndFlush(message)
        } else {
            connect(peer.address).addListener { write(message) }

            logger.warn("Message for peer without connection {}. Connection will be created.", message)
        }
    }

    override fun write(message: PeerAndMessage) {
        logger.debug("ConnectionManager accept message {} from {} to send",
                message.message.javaClass.simpleName, message.peer.address)

        outgoing.put(message)
    }

    override fun read(): PeerAndMessage = incoming.take()

    private fun connect(address: InetSocketAddress): ChannelFuture {
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

