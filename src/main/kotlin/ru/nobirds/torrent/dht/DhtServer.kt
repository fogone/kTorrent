package ru.nobirds.torrent.dht

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.handler.codec.MessageToMessageCodec
import ru.nobirds.torrent.bencode.*
import ru.nobirds.torrent.dht.message.Message
import ru.nobirds.torrent.dht.message.bencode.BencodeMessageSerializer
import ru.nobirds.torrent.utils.channelInitializerHandler
import ru.nobirds.torrent.utils.infiniteLoopThread
import java.io.Closeable
import java.net.InetSocketAddress
import java.util.concurrent.ArrayBlockingQueue

public class MessageToDatagramCodec(val messageSerializer: BencodeMessageSerializer) :
        MessageToMessageCodec<DatagramPacket, AddressAndMessage>(DatagramPacket::class.java, AddressAndMessage::class.java) {

    private val buffer = Unpooled.buffer(5000)

    override fun encode(ctx: ChannelHandlerContext, msg: AddressAndMessage, out: MutableList<Any>) {
        val bMap = messageSerializer.serialize(msg.message)

        buffer.clear()

        BTokenBufferWriter(buffer).write(bMap)

        out.add(DatagramPacket(buffer, msg.address))
    }

    override fun decode(ctx: ChannelHandlerContext, msg: DatagramPacket, out: MutableList<Any>) {
        val content = msg.content()
        val sender = msg.sender()
        val stream = BTokenStreamImpl(BufferByteReader(content))

        stream.next()

        val bType = stream.processBType()

        val message = messageSerializer.deserialize(sender, bType as BMap)

        out.add(AddressAndMessage(sender, message))
    }
}

public class NettyDhtServer(val port:Int, val messageSerializer: BencodeMessageSerializer) : Closeable {

    private val workerGroup = NioEventLoopGroup()

    private val incoming = ArrayBlockingQueue<AddressAndMessage>(1000)
    private val outgoing = ArrayBlockingQueue<AddressAndMessage>(1000)

    private val server = Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel::class.java)
            .option(ChannelOption.SO_BROADCAST, true)
            .channelInitializerHandler<Channel, Bootstrap> {
                it.pipeline()
                        .addLast(MessageToDatagramCodec(messageSerializer))
                        .addLast(requestQueueStorage(incoming))
            }
            .bind(port)

    private val sender = infiniteLoopThread {
        server.channel().writeAndFlush(outgoing.take())
    }

    public fun read():AddressAndMessage = incoming.take()

    public fun send(addressAndMessage: AddressAndMessage) {
        outgoing.put(addressAndMessage)
    }

    public fun send(address:InetSocketAddress, message: Message) {
        outgoing.put(AddressAndMessage(address, message))
    }

    override fun close() {
        sender.interrupt()
        workerGroup.shutdownGracefully()
        server.channel().closeFuture().sync()
    }
}