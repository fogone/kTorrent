package ru.nobirds.torrent.peers.provider

import ru.nobirds.torrent.peers.PeerEvent
import ru.nobirds.torrent.peers.PeerListener
import ru.nobirds.torrent.peers.PeerListenerWrapper
import ru.nobirds.torrent.utils.Id

public interface PeerProvider {

    fun require(hash: Id, listener: PeerListener)

    fun require(hash: Id, listener: (PeerEvent)-> Unit) {
        require(hash, PeerListenerWrapper(listener))
    }

    fun needless(hash: Id)
}