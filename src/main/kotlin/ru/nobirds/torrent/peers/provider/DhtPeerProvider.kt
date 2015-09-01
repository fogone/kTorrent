package ru.nobirds.torrent.peers.provider

import ru.nobirds.torrent.dht.Dht
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.peers.PeerEvent
import ru.nobirds.torrent.utils.Id

public class DhtPeerProvider(localPeer: Peer, val dht:Dht) : AbstractPeerProvider(localPeer) {

    override fun onHashRequired(hash: Id) {
        dht.findPeersForHash(hash) { node ->
            notifyPeerEvent(PeerEvent(hash, hashSetOf(node)))
        }
    }

    override fun onNoHashNeeded(hash: Id) {
        // todo!!!
    }

}