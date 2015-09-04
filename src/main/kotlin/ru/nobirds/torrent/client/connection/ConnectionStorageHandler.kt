package ru.nobirds.torrent.client.connection

import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import java.net.InetSocketAddress
import java.net.SocketAddress

public class ConnectionStorageHandler(val registry: ConnectionRegistry) : ChannelHandlerAdapter() {

    override fun connect(ctx: ChannelHandlerContext, remoteAddress: SocketAddress, localAddress: SocketAddress?, promise: ChannelPromise) {
        super.connect(ctx, remoteAddress, localAddress, promise)
        registry.register(remoteAddress, ctx.channel())
    }

    override fun disconnect(ctx: ChannelHandlerContext, promise: ChannelPromise) {
        registry.unregister(ctx.channel().remoteAddress() as InetSocketAddress)
        super.disconnect(ctx, promise)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
        registry.unregister(ctx.channel().remoteAddress())
    }

}