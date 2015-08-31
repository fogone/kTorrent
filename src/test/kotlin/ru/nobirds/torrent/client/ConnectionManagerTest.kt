package ru.nobirds.torrent.client

import org.junit.Assert
import org.junit.Test
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BMessage
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.client.connection.NettyConnectionManager
import java.net.InetSocketAddress
import kotlin.concurrent.thread

public class ConnectionManagerTest {

    @Test
    public fun test1() {
        val manager1 = NettyConnectionManager(6500)
        val manager2 = NettyConnectionManager(6501)

        try {
            val source = BTypeFactory.createBMap {
                value("hello", "world")
            }

            manager1.send(BMessage(InetSocketAddress(6501), source))

            val message = manager2.read()

            Assert.assertEquals(source.getString("hello"), (message.value as BMap).getString("hello"))
        } finally {
            manager1.close()
            manager2.close()
        }
    }

    @Test
    public fun test2() {
        val manager1 = NettyConnectionManager(6500)
        val manager2 = NettyConnectionManager(6501)

        val thread1 = thread {
            repeat(10) {
                manager1.send(BMessage(InetSocketAddress(6501), BTypeFactory.createBMap {
                    value("hello", "world")
                }))
            }
        }

        val thread2 = thread {
            var counter = 0
            while(counter < 10) {
                val message = manager2.read()
                Assert.assertEquals(true, message.value is BMap)
                val map:BMap = message.value as BMap
                Assert.assertEquals("world", map.getString("hello"))
                counter++
            }
        }

        thread1.join()
        thread2.join()

        manager1.close()
        manager2.close()
    }

}