package ru.nobirds.torrent.client.connection

import io.netty.channel.Channel
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap

class ConnectionRegistry {

    private val connections = ConcurrentHashMap<InetSocketAddress, Channel>()

    fun register(peer: SocketAddress, context: Channel) {
        connections.put(peer as InetSocketAddress, context)
    }

    fun unregister(peer: SocketAddress) {
        connections.remove(peer)
    }

    fun find(peer: SocketAddress): Channel? = connections.get(peer)

}