package ru.nobirds.torrent.bencode

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.StringWriter
import java.io.Writer

interface BType {

    fun process(stream: BTokenStream)

    val startPosition:Long

    val endPosition:Long

    fun <T:Writer> toString(writer:T):T {
        BTypeFormatter(writer).format(this)
        return writer
    }

    fun toString(stream:OutputStream) {
        toString(OutputStreamWriter(stream))
    }
}