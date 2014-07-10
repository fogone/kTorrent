package ru.nobirds.torrent.client



import java.security.SecureRandom
import java.net.InetAddress
import java.net.InetSocketAddress

public class LocalPeerFactory(val digest: DigestProvider) {

    private val random = SecureRandom()

    public fun createLocalPeer(portRange:LongRange):Peer {
        val port = findFreePort(portRange)

        if(port == null)
            throw IllegalStateException("All configured ports used.")

        val peerId = createPeerId()

        return Peer(peerId, InetSocketAddress(InetAddress.getLocalHost(), port.toInt()))
    }

    private fun createPeerId():ByteArray {
        val bytes = ByteArray(128)

        random.nextBytes(bytes)

        return digest.encode(bytes)
    }

    private fun findFreePort(portRange:LongRange):Long? {
        return portRange.firstOrNull { it.toInt().isPortAvailable() }
    }

}