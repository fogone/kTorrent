package ru.nobirds.torrent.dht.message

import ru.nobirds.torrent.bencode.BMap
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress

public interface MessageSerializer {

    fun deserialize(address:InetSocketAddress, source:BMap):Message

    fun serialize(message:Message):BMap

}