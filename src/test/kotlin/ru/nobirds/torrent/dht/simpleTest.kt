package ru.nobirds.torrent.dht

import org.junit.Test as test
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.toHexString

fun main(args: Array<String>) {
    val dht = Dht(11111)

    dht.findPeersForHash(Id.fromHexString("944D2E1C1443008DDFA34A89AEC282393AC8D883")) { address ->
        println(address.toString())
    }

    dht.findPeersForHash(Id.fromHexString("944D2E1C1443008DDFA34A89AEC282393AC8D883")) { address ->
        println(address.toString())
    }

    dht.server.join()
}