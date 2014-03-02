package ru.nobirds.torrent.client.parser

import java.io.InputStream
import java.io.OutputStream
import org.springframework.stereotype.Service as service
import ru.nobirds.torrent.bencode.BTokenInputStream
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BTokenOutputStream
import java.io.ByteArrayOutputStream

public service class BencoderService {

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

    public fun encode(map:Map<String, Any>):ByteArray {
        val result = ByteArrayOutputStream()
        encode(result, map)
        return result.toByteArray()
    }

}