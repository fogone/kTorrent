package ru.nobirds.torrent.dht

import java.net.InetSocketAddress
import ru.nobirds.torrent.dht.message.Message

public data class AddressAndMessage(val address: InetSocketAddress, val message: Message)