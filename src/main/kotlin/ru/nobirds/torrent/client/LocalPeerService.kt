package ru.nobirds.torrent.client

import java.net.InetAddress
import java.net.NetworkInterface
import java.net.Inet4Address
import org.springframework.beans.factory.annotation.Autowired as autowired
import ru.nobirds.torrent.config.Config
import java.net.ServerSocket
import java.net.SocketException
import org.springframework.stereotype.Service as service
import kotlin.properties.Delegates
import javax.annotation.PostConstruct

public service class LocalPeerService {

    private autowired var config:Config? = null

    public var localPeer:Peer by Delegates.notNull()

    PostConstruct
    public fun init() {
        localPeer = createLocalPeer()
    }

    private fun createLocalPeer():Peer {

        val localAddresses = findLocalAddresses()

        if(localAddresses.isEmpty())
            throw IllegalStateException("No local addresses found.")

        val portRange = config!!.get(ClientProperties.clientPortsRange)

        val port = findFreePort(portRange)

        if(port == null)
            throw IllegalStateException("All configured ports used.")

        val peerId = config!!.get(ClientProperties.peerId)

        return Peer(peerId, localAddresses.first!!, port.toInt())
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