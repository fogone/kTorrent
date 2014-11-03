package ru.nobirds.torrent.dht

import java.util.concurrent.ConcurrentHashMap
import ru.nobirds.torrent.utils.TokenGenerator
import java.util.concurrent.CopyOnWriteArrayList
import java.util.Collections
import ru.nobirds.torrent.utils.toPriorityQueue
import ru.nobirds.torrent.utils.top
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.peers.Peer
import java.net.InetSocketAddress
import java.util.TreeMap
import java.util.Comparator

public class DistributedMap {

    private val values = ConcurrentHashMap<Id, MutableList<InetSocketAddress>>()

    private val peers = ConcurrentHashMap<Id, Peer>()

    public fun addNode(peer: Peer) {
        peers.put(peer.id, peer)
    }

    public fun addNodes(peers: Iterable<Peer>) {
        for (peer in peers) {
            addNode(peer)
        }
    }

    fun removeNode(key: Id) {
        peers.remove(key)
    }

    public fun put(key: Id, values: Iterable<InetSocketAddress>) {
        this.values.getOrPut(key) { CopyOnWriteArrayList() }.addAll(values)
    }

    public fun get(key: Id):List<InetSocketAddress> = values.getOrElse(key) { Collections.emptyList<InetSocketAddress>() }

    public fun contains(key: Id): Boolean = values.containsKey(key)

    public fun findClosest(key: Id, count:Int = 8):List<Peer> = when {
        peers.size == 0 -> Collections.emptyList()
        peers.size == 1 -> Collections.singletonList(peers.values().first())
        else -> peers.entrySet().toPriorityQueue { (it.key xor key).toBigInteger() }.top(count).map { it.value }
    }

}