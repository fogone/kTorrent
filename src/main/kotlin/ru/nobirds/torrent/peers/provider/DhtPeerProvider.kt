package ru.nobirds.torrent.peers.provider

import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.dht.Dht
import ru.nobirds.torrent.peers.PeerEvent
import ru.nobirds.torrent.peers.Peer

public class DhtPeerProvider(localPeer: Peer) : AbstractPeerProvider(localPeer) {

    private val dht = Dht(localPeer.address.getPort())

    override fun onHashRequired(hash: Id) {
        dht.findPeersForHash(hash) { node ->
            notifyPeerEvent(PeerEvent(hash, hashSetOf(node)))
        }
    }

    override fun onNoHashNeeded(hash: Id) {
        // do nothing
    }


}