package ru.nobirds.torrent.bencode

import java.io.InputStream

public object Bencoder {

    public fun bencode(stream:InputStream):Map<String, Any> {
        val tokenInputStream = BTokenInputStream(stream)

        tokenInputStream.next()

        val map = tokenInputStream.processBType() as BMap

        return map.value
    }

}