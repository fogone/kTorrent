package ru.nobirds.torrent.parser

import ru.nobirds.torrent.bencode.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

object Bencoder {

    fun decodeBMap(stream:InputStream):BMap {
        val tokenInputStream = BTokenStreamImpl(StreamByteReader(stream))

        tokenInputStream.next()

        return tokenInputStream.processBType() as BMap
    }

    fun encodeBType(stream:OutputStream, value:BType) {
        val tokenOutputStream = BTokenOutputStream(stream)
        tokenOutputStream.writeBObject(value)
        stream.flush()
    }

    fun encodeBType(value:BType):ByteArray {
        val result = ByteArrayOutputStream()
        encodeBType(result, value)
        return result.toByteArray()
    }

    fun encodeBTypes(stream:OutputStream, values:Iterable<BType>) {
        val tokenOutputStream = BTokenOutputStream(stream)
        for (value in values) {
            tokenOutputStream.writeBObject(value)
        }
        stream.flush()
    }

    fun encodeBTypes(values:Iterable<BType>):ByteArray {
        val result = ByteArrayOutputStream()
        encodeBTypes(result, values)
        return result.toByteArray()
    }
}