package ru.nobirds.torrent.kademlia

import java.util.concurrent.ConcurrentHashMap
import ru.nobirds.torrent.utils.TokenGenerator
import java.util.concurrent.CopyOnWriteArrayList
import java.util.Collections
import ru.nobirds.torrent.utils.toPriorityQueue
import ru.nobirds.torrent.utils.top
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.peers.Peer

data class TokenPair(val peer: Id, var myToken:String? = null, var peerToken:String = TokenGenerator.generate())

public class PeersContainer {

    private val tokens = ConcurrentHashMap<Id, TokenPair>()

    private val peers = ConcurrentHashMap<Id, MutableList<Peer>>()

    private val peersListeners = ConcurrentHashMap<Id, MutableList<(Peer)->Unit>>()

    public fun checkPeerToken(sender: Peer, token:String):Boolean {
        val tokenPair = tokens[sender.id]?.peerToken

        return tokenPair != null && token == tokenPair
    }

    public fun hasToken(peer: Id):Boolean = getToken(peer) != null

    public fun getToken(peer: Id):String? = tokens[peer]?.myToken

    public fun setToken(peer: Id, token:String) {
        tokens.getOrPut(peer) { TokenPair(peer) }.myToken = token
    }

    public fun getPeerToken(peer: Id):String = tokens.getOrPut(peer) { TokenPair(peer) }.peerToken

    public fun registerPeerListener(hash: Id, listener:(Peer)->Unit) {
        peers[hash]?.forEach { listener(it) }
        peersListeners.getOrPut(hash) { CopyOnWriteArrayList() }.add(listener)
    }

    public fun notifyPeer(hash: Id, node: Peer, token:String) {
        setToken(node.id, token)
        peers.getOrPut(hash) { CopyOnWriteArrayList() }.add(node)
        peersListeners[hash]?.forEach { it(node) }
    }

    public fun notifyPeerGone(hash: Id, node: Peer) {
        peers[hash]?.remove(node)
    }

    public fun find(hash: Id):List<Peer> = peers.getOrElse(hash) { Collections.emptyList<Peer>() }

    public fun findClosest(hash: Id, count:Int = 8):List<Peer>
            = peers.values().flatMap { it }.toPriorityQueue { (it.id xor hash).toBigInteger() }.top(count)

}