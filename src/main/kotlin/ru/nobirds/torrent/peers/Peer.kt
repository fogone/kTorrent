package ru.nobirds.torrent.peers

import ru.nobirds.torrent.utils.Id
import java.net.InetSocketAddress

data class Peer(val hash:Id, val address: InetSocketAddress)