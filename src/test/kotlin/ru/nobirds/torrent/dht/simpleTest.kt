package ru.nobirds.torrent.dht

import org.junit.Test
import ru.nobirds.torrent.utils.Id
import java.util.concurrent.ConcurrentHashMap
import org.junit.Test as test

public class DhtTest {

    @Test
    public fun test1() {
        val dht = Dht(11111L..111113L)

        val peers = ConcurrentHashMap<String, Boolean>()

        dht.findPeersForHash(Id.fromHexString("944D2E1C1443008DDFA34A89AEC282393AC8D883")) { address ->
            val addressString = address.toString()

            if (!peers.containsKey(addressString)) {
                peers.put(addressString, true)
                println(addressString)
            }
        }

        Thread.sleep(1000000)
    }
}