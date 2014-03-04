package ru.nobirds.torrent.client

import java.net.InetAddress
import java.net.InetSocketAddress

public data class Peer(val id:String, val address:InetSocketAddress)