package ru.nobirds.torrent.bencode

import java.io.StringWriter
import java.io.Writer
import java.io.OutputStream
import java.io.OutputStreamWriter

public trait BType {

    fun process(stream: BTokenInputStream)

    val startPosition:Long

    val endPosition:Long

    override fun toString(): String {
        return toString(StringWriter()).toString()
    }

    fun toString<T:Writer>(writer:T):T {
        BTypeFormatter(writer).format(this)
        return writer
    }

    fun toString(stream:OutputStream) {
        toString(OutputStreamWriter(stream))
    }
}