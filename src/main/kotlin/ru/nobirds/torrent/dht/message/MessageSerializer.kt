package ru.nobirds.torrent.dht.message

import ru.nobirds.torrent.bencode.BMap
import java.net.InetSocketAddress

public interface MessageSerializer {

    fun deserialize(address:InetSocketAddress, map:BMap):Message

    fun serialize(message:Message):BMap

}