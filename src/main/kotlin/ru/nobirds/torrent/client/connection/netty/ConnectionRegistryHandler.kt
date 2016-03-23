package ru.nobirds.torrent.client.connection.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.ipfilter.AbstractRemoteAddressFilter
import ru.nobirds.torrent.utils.log
import java.net.InetSocketAddress

class ConnectionRegistryHandler(val registry: ConnectionRegistry) : AbstractRemoteAddressFilter<InetSocketAddress>() {

    private val logger = log()

    override fun accept(ctx: ChannelHandlerContext?, remoteAddress: InetSocketAddress?): Boolean = true // todo?

    override fun channelAccepted(ctx: ChannelHandlerContext, remoteAddress: InetSocketAddress) {
        register(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.warn("Communication problem with peer ${ctx.channel().remoteAddress()}: ${cause.message}")
        logger.debug("Exception with ${ctx.channel().remoteAddress()}", cause)

        // unregister(ctx)
        ctx.close()

        super.exceptionCaught(ctx, cause)
    }

    private fun register(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()

        if (channel.remoteAddress() != null) {
            registry.register(channel)
        } else {
            logger.warn("Can't register channel, cause: no remote address")
        }
    }

    private fun unregister(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()

        if (channel.remoteAddress() != null) {
            registry.unregister(channel)
        } else {
            logger.warn("Can't unregister channel, cause: no remote address")
        }
    }

}