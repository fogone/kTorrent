package ru.nobirds.torrent.bencode

import kotlin.properties.Delegates

class BKeyValuePair() : BType {

    private var bpair:Pair<String, BType> by Delegates.notNull()

    fun set(name:String, value:BType):BKeyValuePair {
        bpair = name to value
        return this
    }

    override fun process(stream: BTokenStream) {
        startPosition = stream.position()

        val name = stream.processBType() as BBytes

        stream.next()

        val value = stream.processBType()

        endPosition = stream.position() + 1

        val nameAsString = name.toString()

        bpair = Pair(nameAsString, value)
    }

    val value:BType
        get() = bpair.second

    val name:String
        get() = bpair.first

    override var startPosition: Long = 0L
    override var endPosition: Long = 0L

}