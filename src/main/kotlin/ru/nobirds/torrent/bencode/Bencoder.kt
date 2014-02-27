package ru.nobirds.torrent.bencode

import java.io.InputStream
import java.io.OutputStream

public object Bencoder {

    public fun decode(stream:InputStream):Map<String, Any> {
        val tokenInputStream = BTokenInputStream(stream)

        tokenInputStream.next()

        val map = tokenInputStream.processBType() as BMap

        return map.value
    }

    public fun encode(stream:OutputStream, map:Map<String, Any>) {
        val tokenOutputStream = BTokenOutputStream(stream)
        tokenOutputStream.write(map)
        stream.flush()
    }
}