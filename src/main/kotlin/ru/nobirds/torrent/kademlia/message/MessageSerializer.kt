package ru.nobirds.torrent.kademlia.message

import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress

public trait MessageSerializer {

    fun deserialize(address:InetSocketAddress, source:InputStream):Message

    fun serialize(message:Message, output:OutputStream)

}