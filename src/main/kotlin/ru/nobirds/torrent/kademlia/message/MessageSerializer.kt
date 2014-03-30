package ru.nobirds.torrent.kademlia.message

import java.io.InputStream
import java.io.OutputStream

public trait MessageSerializer {

    fun deserialize(source:InputStream):Message

    fun serialize(message:Message, output:OutputStream)

}