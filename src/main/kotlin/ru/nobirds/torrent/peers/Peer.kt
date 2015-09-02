package ru.nobirds.torrent.peers

import ru.nobirds.torrent.utils.Id
import java.net.InetSocketAddress

public data class Peer(val hash:Id, val id:Id, val address: InetSocketAddress)