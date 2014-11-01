package ru.nobirds.torrent.peers

import java.net.InetSocketAddress
import ru.nobirds.torrent.utils.Id

public data class Peer(val id:Id, val address: InetSocketAddress)