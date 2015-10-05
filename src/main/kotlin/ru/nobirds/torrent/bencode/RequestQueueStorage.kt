package ru.nobirds.torrent.bencode

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.util.concurrent.BlockingQueue
import kotlin.reflect.KClass

public inline fun <reified T:Any> requestQueueStorage(incoming: BlockingQueue<T>): ChannelHandler = RequestQueueStorage<T>(incoming, T::class)

public class RequestQueueStorage<T:Any>(val incoming: BlockingQueue<T>, val type:KClass<T>) :
        SimpleChannelInboundHandler<T>(type.java, false) {

    override fun messageReceived(ctx: ChannelHandlerContext, msg: T) {
        incoming.put(msg)
    }

}
