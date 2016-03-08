package ru.nobirds.torrent.dht

import org.junit.Ignore
import org.junit.Test
import ru.nobirds.torrent.utils.Id
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import org.junit.Test as test

class DhtTest {

    @Test
    @Ignore fun test1() {
        val dht = Dht(11111, sequenceOf())
        dht.announce(Id.fromHexString("944D2E1C1443008DDFA34A89AEC282393AC8D883"))
        dht.makeInitialized()

        val dht2 = Dht(11112, sequenceOf(InetSocketAddress(11111)))

        val peers = ConcurrentHashMap<String, Boolean>()

        dht2.findPeersForHash(Id.fromHexString("944D2E1C1443008DDFA34A89AEC282393AC8D883")) { address ->
            address.equals(InetSocketAddress(11111))


        }

        Thread.sleep(10000)

        dht.close()
        dht2.close()
    }
}