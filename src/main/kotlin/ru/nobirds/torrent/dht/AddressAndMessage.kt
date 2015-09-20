package ru.nobirds.torrent.dht

import ru.nobirds.torrent.dht.message.DhtMessage
import java.net.InetSocketAddress

public data class AddressAndMessage(val address: InetSocketAddress, val message: DhtMessage)