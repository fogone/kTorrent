package ru.nobirds.torrent.utils

import io.netty.bootstrap.AbstractBootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelInitializer
import io.netty.util.Attribute


public fun ChannelFuture.addCompleteListener(listener:(ChannelFuture)->Unit) {
    addListener(object: ChannelFutureListener {
        override fun operationComplete(future: ChannelFuture) {
            if(future.isSuccess)
                listener(future)
        }
    })
}

public fun <C: Channel> ServerBootstrap.childHandler(initializer:(C)->Unit): ServerBootstrap =
        childHandler(channelInitializer(initializer))

public fun <C: Channel, B: AbstractBootstrap<B, C>> B.channelInitializerHandler(initializer:(C)->Unit):B =
        handler(channelInitializer(initializer))

public fun <C: Channel> channelInitializer(initializer:(C)->Unit): ChannelInitializer<C> = object: ChannelInitializer<C>() {
    override fun initChannel(channel: C) {
        initializer(channel)
    }
}

public fun <T> Attribute<T>.getOrSet(getter:()->T):T {
    if (get() == null) {
        set(getter())
    }
    return get()
}