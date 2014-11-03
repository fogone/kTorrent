package ru.nobirds.torrent.peers

import ru.nobirds.torrent.utils.Id
import java.util.Collections
import java.net.InetSocketAddress

public data class PeerEvent(val hash: Id, val peers: Set<InetSocketAddress>)