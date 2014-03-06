package ru.nobirds.torrent.client

import java.net.InetSocketAddress

public data class Peer(val id:ByteArray, val address:InetSocketAddress)