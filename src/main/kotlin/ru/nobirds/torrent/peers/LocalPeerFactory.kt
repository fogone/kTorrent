package ru.nobirds.torrent.peers

import ru.nobirds.torrent.utils.Id
import java.net.InetSocketAddress

class LocalPeerFactory(val port:Int) {

    fun createLocalPeer(hash:Id): Peer
            = Peer(hash, InetSocketAddress(port.toInt()))

}

