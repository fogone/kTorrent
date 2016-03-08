package ru.nobirds.torrent.dht.message

import ru.nobirds.torrent.bencode.BMap

interface MessageSerializer {

    fun deserialize(map:BMap): DhtMessage

    fun serialize(message: DhtMessage):BMap

}