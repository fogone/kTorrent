package ru.nobirds.torrent.peers

import ru.nobirds.torrent.utils.Id
import java.net.InetSocketAddress

data class PeerEvent(val hash: Id, val peers: Set<InetSocketAddress>)