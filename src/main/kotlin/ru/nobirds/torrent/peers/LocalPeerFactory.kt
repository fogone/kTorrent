package ru.nobirds.torrent.peers

import java.net.InetAddress
import java.net.InetSocketAddress
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.isPortAvailable

public class LocalPeerFactory(val portRange:LongRange) {

    public fun createLocalPeer(): Peer {
        val port = findFreePort(portRange)

        if(port == null)
            throw IllegalStateException("All configured ports used.")

        return Peer(Id.random(), InetSocketAddress(InetAddress.getLocalHost(), port.toInt()))
    }

    private fun findFreePort(portRange:LongRange):Long? {
        return portRange.firstOrNull { it.toInt().isPortAvailable() }
    }

}

