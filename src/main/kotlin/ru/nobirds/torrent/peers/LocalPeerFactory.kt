package ru.nobirds.torrent.peers

import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.isPortAvailable
import java.net.InetSocketAddress

public class LocalPeerFactory(val portRange:LongRange) {

    public val port:Int = findFreePort(portRange)?.toInt() ?: throw IllegalStateException("All configured ports used.")

    public fun createLocalPeer(hash:Id): Peer
            = Peer(hash, Id.random(), InetSocketAddress(port.toInt()))

    private fun findFreePort(portRange:LongRange):Long?
            = portRange.firstOrNull { it.toInt().isPortAvailable() }

}

