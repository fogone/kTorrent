package ru.nobirds.torrent.parser

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

    public fun encodeBType(stream:OutputStream, value:BType) {
        val tokenOutputStream = BTokenOutputStream(stream)
        tokenOutputStream.writeBObject(value)
        stream.flush()
    }

    public fun encodeBType(value:BType):ByteArray {
        val result = ByteArrayOutputStream()
        encodeBType(result, value)
        return result.toByteArray()
    }

    public fun encodeBTypes(stream:OutputStream, values:Iterable<BType>) {
        val tokenOutputStream = BTokenOutputStream(stream)
        for (value in values) {
            tokenOutputStream.writeBObject(value)
        }
        stream.flush()
    }

    public fun encodeBTypes(values:Iterable<BType>):ByteArray {
        val result = ByteArrayOutputStream()
        encodeBTypes(result, values)
        return result.toByteArray()
    }
}