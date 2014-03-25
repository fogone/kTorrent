package ru.nobirds.torrent.kademlia.message

import java.io.InputStream
import java.io.OutputStream

public trait MessageSerializer {

    fun serialize(source:InputStream):Message

    fun deserialize(message:Message, output:OutputStream)

}