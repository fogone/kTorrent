package ru.nobirds.torrent.kademlia.message

import java.io.OutputStream
import java.io.InputStream

public class GsonMessageSerializer() : MessageSerializer {

    override fun serialize(source: InputStream): Message {
        throw UnsupportedOperationException()
    }

    override fun deserialize(message: Message, output: OutputStream) {
        throw UnsupportedOperationException()
    }

}