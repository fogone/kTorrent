package ru.nobirds.torrent.peers

import ru.nobirds.torrent.utils.Id
import java.net.InetSocketAddress

public class LocalPeerFactory(val port:Int) {

    public fun createLocalPeer(hash:Id): Peer
            = Peer(hash, Id.random(), InetSocketAddress(port.toInt()))

}

