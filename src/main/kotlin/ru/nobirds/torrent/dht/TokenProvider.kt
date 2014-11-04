package ru.nobirds.torrent.dht

import java.util.concurrent.ConcurrentHashMap
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.TokenGenerator
import java.util.Random
import java.util.UUID

public class TokenProvider {

    private val tokens = ConcurrentHashMap<Id, TokenPair>()

    private val localToken: String = generateToken()

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

    public fun getLocalToken(): String = localToken // todo: regenerate me

    public fun generateToken():String = UUID.randomUUID().toString()

}

data class TokenPair(val peer: Id, var myToken:String? = null, var peerToken:String = TokenGenerator.generate())