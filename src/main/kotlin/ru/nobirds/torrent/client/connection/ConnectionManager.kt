package ru.nobirds.torrent.client.connection

import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.peers.Peer
import java.io.Closeable

data class PeerAndMessage(val peer: Peer, val message: Message)

interface ConnectionManager : Closeable {

    fun write(message: PeerAndMessage)

    fun write(peer: Peer, message: Message) {
        write(PeerAndMessage(peer, message))
    }

    fun read(): PeerAndMessage

}

