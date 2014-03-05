package ru.nobirds.torrent.client.parser

import java.io.InputStream
import java.io.OutputStream

import ru.nobirds.torrent.bencode.BTokenInputStream
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BTokenOutputStream
import java.io.ByteArrayOutputStream
import ru.nobirds.torrent.bencode.BType

public object Bencoder {

    public fun decodeBMap(stream:InputStream):BMap {
        val tokenInputStream = BTokenInputStream(stream)

        tokenInputStream.next()

        return tokenInputStream.processBType() as BMap
    }

    public fun decode(stream:InputStream):Map<String, Any> {
        return decodeBMap(stream).value
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

    public fun encodeBType(stream:OutputStream, value:BType<out Any>) {
        val tokenOutputStream = BTokenOutputStream(stream)
        tokenOutputStream.writeBObject(value)
        stream.flush()
    }

    public fun encodeBType(value:BType<out Any>):ByteArray {
        val result = ByteArrayOutputStream()
        encodeBType(result, value)
        return result.toByteArray()
    }
}