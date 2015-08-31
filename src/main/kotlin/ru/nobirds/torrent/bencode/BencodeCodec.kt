package ru.nobirds.torrent.bencode

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.ByteToMessageCodec
import java.net.SocketAddress
import java.util.concurrent.BlockingQueue

public data class BMessage(val address: SocketAddress, val value:BType)

public class BencodeCodec() : ByteToMessageCodec<BType>(BType::class.java) {

    override fun encode(ctx: ChannelHandlerContext, msg: BType, out: ByteBuf) {
        BTokenBufferWriter(out).writeBObject(msg)
    }

    override fun decode(ctx: ChannelHandlerContext, buffer: ByteBuf, out: MutableList<Any>) {
        val stream = BTokenStreamImpl(BufferByteReader(buffer))

        stream.next()

        out.add(stream.processBType())

        if(buffer.readableBytes() > 0)
            buffer.readerIndex(buffer.readerIndex()-1)
    }

}

class BencodeRequestQueueStorage(val incoming: BlockingQueue<BMessage>) :
        SimpleChannelInboundHandler<BType>(BType::class.java, true) {

    override fun messageReceived(ctx: ChannelHandlerContext, msg: BType) {
        incoming.put(BMessage(ctx.channel().remoteAddress(), msg))
    }

}
