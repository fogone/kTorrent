package ru.nobirds.torrent.dht

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerAppender
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.handler.codec.ByteToMessageCodec
import io.netty.handler.codec.MessageToMessageCodec
import io.netty.util.AttributeKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.nobirds.torrent.bencode.*
import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.dht.message.DefaultRequestContainer
import ru.nobirds.torrent.dht.message.DhtMessage
import ru.nobirds.torrent.dht.message.RequestMessage
import ru.nobirds.torrent.dht.message.ResponseMessage
import ru.nobirds.torrent.dht.message.bencode.BencodeMessageSerializer
import ru.nobirds.torrent.utils.channelInitializerHandler
import ru.nobirds.torrent.utils.log
import ru.nobirds.torrent.utils.queueHandlerThread
import java.io.Closeable
import java.net.InetSocketAddress
import java.util.concurrent.ArrayBlockingQueue

public class DatagramToDhtMessage(val messageSerializer: BencodeMessageSerializer) :
        MessageToMessageCodec<DatagramPacket, AddressAndMessage>(DatagramPacket::class.java, AddressAndMessage::class.java) {

    private val logger = log()

    override fun decode(ctx: ChannelHandlerContext, msg: DatagramPacket, out: MutableList<Any>) {
        val message = decode(msg)
        val addressAndMessage = AddressAndMessage(msg.sender(), message)

        logger.debug("Received message {} from {}", addressAndMessage.message.javaClass.simpleName, addressAndMessage.address)

        out.add(addressAndMessage)
    }

    private fun decode(msg: DatagramPacket): DhtMessage {
        val map = bytesToBMap(msg.content())
        return messageSerializer.deserialize(map)
    }

    private fun bytesToBMap(content: ByteBuf): BMap {
        val stream = BTokenStreamImpl(BufferByteReader(content))
        stream.next()
        return stream.processBType() as BMap
    }

    override fun encode(ctx: ChannelHandlerContext, msg: AddressAndMessage, out: MutableList<Any>) {
        val bytes = encode(msg)

        logger.debug("Sent message {} to {}", msg.message.messageType.toString(), msg.address)

        out.add(DatagramPacket(bytes, msg.address))
    }

    private fun encode(msg: AddressAndMessage): ByteBuf {
        val map = messageSerializer.serialize(msg.message)
        return bMapToBytes(map)
    }

    private fun bMapToBytes(map: BMap): ByteBuf {
        val buffer = Unpooled.buffer(65 * 1024)
        BTokenBufferWriter(buffer).write(map)
        return buffer
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        super.channelReadComplete(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.debug("Connection to {} closed, cause {}", ctx.channel().remoteAddress(), cause.getMessage())
        ctx.channel().close()
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
            .channelInitializerHandler<Channel, Bootstrap> {
                it.pipeline().addLast(
                        DatagramToDhtMessage(messageSerializer),
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