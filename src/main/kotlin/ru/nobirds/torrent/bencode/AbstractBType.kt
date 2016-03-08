package ru.nobirds.torrent.bencode

import java.io.StringWriter

abstract class AbstractBType : BType {

    abstract fun processChar(stream: BTokenStream):Boolean

    override fun process(stream: BTokenStream) {
        if(processByte(stream))
            return

        while(stream.hasNext()) {
            stream.next()
            if(processByte(stream))
                return
        }
    }

    private fun processByte(stream: BTokenStream):Boolean {
        if(stream.current() < 0)
            throw IllegalStateException()

        return processChar(stream)
    }

    override fun toString(): String {
        return toString(StringWriter()).toString()
    }

}