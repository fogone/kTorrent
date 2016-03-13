package ru.nobirds.torrent.client

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import ru.nobirds.torrent.client.connection.netty.NettyConnectionManager
import ru.nobirds.torrent.client.connection.PeerAndMessage
import ru.nobirds.torrent.client.message.HandshakeMessage
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.Id
import java.net.InetSocketAddress

class ConnectionManagerTest {

    @Test
    @Ignore
    fun test1() {
        val manager1 = NettyConnectionManager(6500)
        val manager2 = NettyConnectionManager(6501)

        try {

            val hash = Id.random()
            val peer = Id.random()

            manager1.write(Peer(hash, InetSocketAddress(6501)), HandshakeMessage(hash, peer, "BTest"))

            val message = manager2.read()

            assertMessage(hash, peer, message)

            val peer2 = Id.random()

            manager2.write(Peer(hash, InetSocketAddress(6500)), HandshakeMessage(hash, peer2, "BTest"))

            val message2 = manager1.read()

            assertMessage(hash, peer2, message2)
        } finally {
            manager1.close()
            manager2.close()
        }
    }

    private fun assertMessage(hash: Id, peer: Id, message: PeerAndMessage) {
        Assert.assertEquals(hash, message.peer.hash)
        Assert.assertTrue(message.message is HandshakeMessage)

        val handshakeMessage = message.message as HandshakeMessage

        Assert.assertEquals(peer, handshakeMessage.peer)
        Assert.assertEquals(hash, handshakeMessage.hash)
        Assert.assertEquals("BTest", handshakeMessage.protocol)
    }

}