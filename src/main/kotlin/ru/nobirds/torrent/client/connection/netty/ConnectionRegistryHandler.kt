package ru.nobirds.torrent.client.connection.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class ConnectionRegistryHandler(val registry: ConnectionRegistry) : ChannelInboundHandlerAdapter() {

    override fun channelRegistered(ctx: ChannelHandlerContext) {
        registry.register(ctx.channel())
        super.channelRegistered(ctx)
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        super.channelUnregistered(ctx)
        registry.unregister(ctx.channel())
    }
}