package ru.nobirds.torrent.client



import java.security.SecureRandom
import java.net.InetAddress
import java.net.InetSocketAddress

public object LocalPeerFactory {

    public fun createLocalPeer(portRange:LongRange):Peer {
        val port = findFreePort(portRange)

        if(port == null)
            throw IllegalStateException("All configured ports used.")

        val peerId = createPeerId()

        return Peer(peerId, InetSocketAddress(InetAddress.getLocalHost(), port.toInt()))
    }

    private fun createPeerId():String {
        val bytes = ByteArray(128)
        SecureRandom().nextBytes(bytes)
        return Sha1Provider.encode(bytes)
    }

    private fun findFreePort(portRange:LongRange):Long? {
        return portRange.find { it.toInt().isPortAvailable() }
    }

}