package ru.nobirds.torrent.dht

import java.net.InetSocketAddress
import ru.nobirds.torrent.dht.message.DhtMessage

public data class AddressAndMessage(val address: InetSocketAddress, val message: DhtMessage)