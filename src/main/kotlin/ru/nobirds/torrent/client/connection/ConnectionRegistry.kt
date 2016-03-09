package ru.nobirds.torrent.client.connection

import io.netty.channel.Channel
import io.netty.util.AttributeKey
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.log
import java.util.concurrent.ConcurrentHashMap

class ConnectionRegistry {

    private val logger = log()

    private val connections = ConcurrentHashMap<Peer, Channel>()

    fun registered(peer: Peer):Boolean = connections.get(peer).run { this != null && this.isOpen }

    companion object {
        private val idAttributeKey = AttributeKey.newInstance<Peer>("id")
    }

    fun register(peer: Peer, context: Channel) {
        context.attr(idAttributeKey).setIfAbsent(peer)

        connections.put(peer, context)

        logger.debug("Connection with peer {} established", peer)
    }

    fun find(peer: Peer): Channel? {
        val channel = connections.get(peer)

        return if(channel != null) {
            if(channel.isOpen) channel
            else {
                unregister(peer)
                null
            }
        } else null
    }

    fun unregister(peer: Peer) {
        logger.debug("Connection with peer {} closed", peer)

        val channel = connections.remove(peer)

        channel?.attr(idAttributeKey)?.remove()

        if (channel != null && channel.isOpen) {
            channel.close()
        }
    }

}