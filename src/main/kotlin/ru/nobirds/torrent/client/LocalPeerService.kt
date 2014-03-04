package ru.nobirds.torrent.client

import java.net.NetworkInterface
import java.net.Inet4Address
import org.springframework.beans.factory.annotation.Autowired as autowired
import ru.nobirds.torrent.config.Config
import org.springframework.stereotype.Service as service
import kotlin.properties.Delegates
import javax.annotation.PostConstruct
import java.security.SecureRandom

public service class LocalPeerService {

    private autowired var config:Config? = null

    public fun createLocalPeer():Peer {
        val localAddresses = findLocalAddresses()

        if(localAddresses.isEmpty())
            throw IllegalStateException("No local addresses found.")

        val portRange = config!!.get(ClientProperties.clientPortsRange)

        val port = findFreePort(portRange)

        if(port == null)
            throw IllegalStateException("All configured ports used.")

        val peerId = createPeerId()

        return Peer(peerId, localAddresses.first!!, port.toInt())
    }

    private fun createPeerId():String {
        val bytes = ByteArray(128)
        SecureRandom().nextBytes(bytes)
        return Sha1Provider.encode(bytes)
    }

    private fun findFreePort(portRange:LongRange):Long? {
        return portRange.find { it.toInt().isPortAvailable() }
    }

    private fun findLocalAddresses():List<String> {
        return NetworkInterface
                .getNetworkInterfaces()!!
                .iterator()
                .flatMap { it.getInetAddresses().iterator() }
                .filter { it is Inet4Address && it.isSiteLocalAddress() }
                .map { it.getHostAddress()!! }
                .filter { !it.equals("127.0.0.1") }
                .toList()
    }
}