package ru.nobirds.torrent.dht

import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.TokenGenerator
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TokenProvider {

    private val tokens = ConcurrentHashMap<Id, TokenPair>()

    private val localToken: String = generateToken()

    fun checkPeerToken(sender: Id, token:String):Boolean {
        val tokenPair = tokens[sender]?.peerToken

        return tokenPair != null && token == tokenPair
    }

    fun hasToken(peer: Id):Boolean = getToken(peer) != null

    fun getToken(peer: Id):String? = tokens[peer]?.myToken

    fun setToken(peer: Id, token:String) {
        tokens.getOrPut(peer) { TokenPair(peer) }.myToken = token
    }

    fun getPeerToken(peer: Id):String = tokens.getOrPut(peer) { TokenPair(peer) }.peerToken

    fun getLocalToken(): String = localToken // todo: regenerate me

    fun generateToken():String = UUID.randomUUID().toString()

}

data class TokenPair(val peer: Id, var myToken:String? = null, var peerToken:String = TokenGenerator.generate())