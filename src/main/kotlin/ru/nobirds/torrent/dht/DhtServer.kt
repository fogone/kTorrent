package ru.nobirds.torrent.dht

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import ru.nobirds.torrent.bencode.*
import ru.nobirds.torrent.utils.channelInitializerHandler
import ru.nobirds.torrent.utils.infiniteLoopThread
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.net.DatagramPacket
import java.net.InetSocketAddress
import java.util.concurrent.ArrayBlockingQueue

public class NettyDht(val port:Int) : Closeable {

    private val localhost = InetSocketAddress(port)
    private val broadcastAddress = InetSocketAddress("255.255.255.255", port)

    private val workerGroup = NioEventLoopGroup()

    private val incoming = ArrayBlockingQueue<BMessage>(1000)
    private val outgoing = ArrayBlockingQueue<BMessage>(1000)

    private val server = Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel::class.java)
            .option(ChannelOption.SO_BROADCAST, true)
            .channelInitializerHandler {
                it.pipeline()
                        .addLast(BencodeCodec())
                        .addLast(BencodeRequestQueueStorage(incoming))
            }
            .bind(port)

    private val sender = infiniteLoopThread {
        val message = outgoing.take()

        val outputStream = ByteArrayOutputStream()
        BTokenOutputStream(outputStream).writeBObject(message.value)
        val byteArray = outputStream.toByteArray()

        server.channel().writeAndFlush(DatagramPacket(
                byteArray, byteArray.size(), broadcastAddress))
    }

    public fun read():BMessage = incoming.take()

    public fun send(address:InetSocketAddress, body:BMap) {
        outgoing.put(BMessage(address, body))
    }

    override fun close() {
        sender.interrupt()
        workerGroup.shutdownGracefully()
        server.channel().closeFuture().sync()
    }
}