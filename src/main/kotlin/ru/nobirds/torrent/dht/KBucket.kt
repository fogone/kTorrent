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
import java.math.BigInteger

public class KBucket(val localId: Id, val k:Int = 100) {

    private val values = ConcurrentHashMap<Id, MutableList<InetSocketAddress>>()

    private val peers = ConcurrentHashMap<Id, Peer>()

    public fun addNode(peer: Peer) {
        if(peers.size >= k) {
            val furthestNodes = peers.find(localId, peers.size - k) { it }
            for (node in furthestNodes) {
                peers.remove(node.id)
            }
        }

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

    public fun getNode(key: Id): Peer = peers.get(key)

    public fun get(key: Id):List<InetSocketAddress> = values.getOrElse(key) { Collections.emptyList<InetSocketAddress>() }

    public fun containsNode(id: Id): Boolean = peers.containsKey(id)

    public fun contains(key: Id): Boolean = values.containsKey(key)

    public fun findClosest(key: Id, count:Int = 8):List<Peer> = when {
        peers.containsKey(key) -> Collections.singletonList(peers.get(key))
        peers.size == 0 -> Collections.emptyList()
        peers.size == 1 -> Collections.singletonList(peers.values().first())
        else -> peers.find(key, count) { it.negate() }
    }

}

fun Map<Id, Peer>.find(key:Id, count:Int, distanceTransformer:(BigInteger)->BigInteger): List<Peer> =
        values().toPriorityQueue { distanceTransformer((it.id xor key).toBigInteger()) }.top(count)