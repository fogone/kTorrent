package ru.nobirds.torrent.dht

import org.junit.Test as test
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.toHexString
import java.util.HashSet
import java.util.concurrent.ConcurrentHashMap
import ru.nobirds.torrent.peers.Peer
import java.net.InetSocketAddress

fun main(args: Array<String>) {
    val dht = Dht(11111)

    val peers = ConcurrentHashMap<String, Boolean>()

    dht.findPeersForHash(Id.fromHexString("944D2E1C1443008DDFA34A89AEC282393AC8D883")) { address ->
        val addressString = address.toString()

        if(!peers.containsKey(addressString)) {
            peers.put(addressString, true)
            println(addressString)
        }
    }

    dht.findPeersForHash(Id.fromHexString("944D2E1C1443008DDFA34A89AEC282393AC8D883")) { address ->
        val addressString = address.toString()

        if(!peers.containsKey(addressString)) {
            peers.put(addressString, true)
            println(addressString)
        }
    }

    dht.server.join()
}