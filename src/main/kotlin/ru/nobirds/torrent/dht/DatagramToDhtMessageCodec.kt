package ru.nobirds.torrent.dht

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageCodec
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BTokenBufferWriter
import ru.nobirds.torrent.bencode.BTokenStreamImpl
import ru.nobirds.torrent.bencode.BufferByteReader
import ru.nobirds.torrent.dht.message.DhtMessage
import ru.nobirds.torrent.dht.message.bencode.BencodeMessageSerializer
import ru.nobirds.torrent.utils.log

public class DatagramToDhtMessageCodec(val messageSerializer: BencodeMessageSerializer) :
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

        logger.debug("Sent message {} to {}", msg.message.javaClass.simpleName, msg.address)

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
        logger.debug("Channel problem {}", cause.getMessage())
    }
}