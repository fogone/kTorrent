package ru.nobirds.torrent.dht

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import ru.nobirds.torrent.bencode.requestQueueStorage
import ru.nobirds.torrent.dht.message.DefaultRequestContainer
import ru.nobirds.torrent.dht.message.DhtMessage
import ru.nobirds.torrent.dht.message.RequestMessage
import ru.nobirds.torrent.dht.message.ResponseMessage
import ru.nobirds.torrent.dht.message.bencode.BencodeMessageSerializer
import ru.nobirds.torrent.utils.channelInitializerHandler
import ru.nobirds.torrent.utils.queueHandlerThread
import java.io.Closeable
import java.net.InetSocketAddress
import java.util.concurrent.ArrayBlockingQueue

public class NettyDhtServer(val port: Int, val messageSerializer: BencodeMessageSerializer, val requestContainer: DefaultRequestContainer) : Closeable {

    private val workerGroup = NioEventLoopGroup()

    private val incoming = ArrayBlockingQueue<AddressAndMessage>(1000)
    private val outgoing = ArrayBlockingQueue<AddressAndMessage>(1000)

    private val server = Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel::class.java)
            .option(ChannelOption.SO_BROADCAST, true)
            .channelInitializerHandler<Channel, Bootstrap> {
                it.pipeline().addLast(
                        DatagramToDhtMessageCodec(messageSerializer),
                        requestQueueStorage<AddressAndMessage>(incoming)
                )
            }
            .bind(port)
            .channel()

    private val sender = queueHandlerThread(outgoing) { sendMessage(it) }

    private fun sendMessage(addressAndMessage:AddressAndMessage) {
        val message = addressAndMessage.message
        if(message is RequestMessage)
            requestContainer.storeWithTimeout(message) {
                send(addressAndMessage) // todo
            }

        server.writeAndFlush(addressAndMessage)
    }

    public fun read():AddressAndMessage {
        val message = incoming.take()

        if(message.message is ResponseMessage)
            requestContainer.cancelById(message.message.id)

        return message
    }

    public fun send(addressAndMessage: AddressAndMessage) {
        outgoing.put(addressAndMessage)
    }

    public fun send(address:InetSocketAddress, message: DhtMessage) {
        outgoing.put(AddressAndMessage(address, message))
    }

    override fun close() {
        sender.interrupt()
        workerGroup.shutdownGracefully()
        server.close()
    }

}