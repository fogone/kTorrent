package ru.nobirds.torrent.client.connection.netty

import io.netty.channel.Channel
import ru.nobirds.torrent.utils.log
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap

class ConnectionRegistry {

    private val logger = log()

    private val connections = ConcurrentHashMap<InetSocketAddress, Channel>()

    fun register(context: Channel) {
        val address = context.remoteAddress()

        connections.put(address as InetSocketAddress, context)

        logger.debug("Connection with peer {} registered", address)
    }

    fun find(address: InetSocketAddress): Channel? {
        return connections[address]
    }

    fun unregister(channel: Channel) {
        val address = channel.remoteAddress()

        logger.debug("Connection with peer {} unregistered", address)

        connections.remove(address)
    }

    operator fun contains(address: SocketAddress): Boolean {
        return connections.containsKey(address)
    }

}