package ru.nobirds.torrent.dht

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerAppender
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.handler.codec.MessageToMessageCodec
import ru.nobirds.torrent.bencode.*
import ru.nobirds.torrent.dht.message.DefaultRequestContainer
import ru.nobirds.torrent.dht.message.DhtMessage
import ru.nobirds.torrent.dht.message.RequestMessage
import ru.nobirds.torrent.dht.message.ResponseMessage
import ru.nobirds.torrent.dht.message.bencode.BencodeMessageSerializer
import ru.nobirds.torrent.utils.queueHandlerThread
import java.io.Closeable
import java.net.InetSocketAddress
import java.util.concurrent.ArrayBlockingQueue

public class MessageToDatagramCodec(val messageSerializer: BencodeMessageSerializer) :
        MessageToMessageCodec<DatagramPacket, AddressAndMessage>(DatagramPacket::class.java, AddressAndMessage::class.java) {

    private val buffer = Unpooled.buffer(65000)

    override fun encode(ctx: ChannelHandlerContext, msg: AddressAndMessage, out: MutableList<Any>) {
        val bMap = messageSerializer.serialize(msg.message)

        buffer.clear()

        BTokenBufferWriter(buffer).write(bMap)

        out.add(DatagramPacket(Unpooled.copiedBuffer(buffer), msg.address))
    }

    override fun decode(ctx: ChannelHandlerContext, msg: DatagramPacket, out: MutableList<Any>) {
        val content = msg.content()
        val stream = BTokenStreamImpl(BufferByteReader(content))

        stream.next()

        val bType = stream.processBType()

        val message = messageSerializer.deserialize(bType as BMap)

        out.add(AddressAndMessage(msg.sender(), message))
    }
}

public class NettyDhtServer(val port: Int, val messageSerializer: BencodeMessageSerializer, val requestContainer: DefaultRequestContainer) : Closeable {

    private val workerGroup = NioEventLoopGroup()

    private val incoming = ArrayBlockingQueue<AddressAndMessage>(1000)
    private val outgoing = ArrayBlockingQueue<AddressAndMessage>(1000)

    private val server = Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel::class.java)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(ChannelHandlerAppender(MessageToDatagramCodec(messageSerializer), requestQueueStorage<AddressAndMessage>(incoming)))
            .bind(port).sync()
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