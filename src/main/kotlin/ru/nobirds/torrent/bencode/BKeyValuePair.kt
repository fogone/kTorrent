package ru.nobirds.torrent.bencode

import kotlin.properties.Delegates

public class BKeyValuePair() : BType<Pair<String, Any>> {

    private var bpair:Pair<String, BType<out Any>> by Delegates.notNull()

    override fun process(stream: BTokenInputStream) {
        startPosition = stream.position()

        val name = stream.processBType() as BBytes

        stream.next()

        val value = stream.processBType()

        endPosition = stream.position() + 1

        val nameAsString = name.toString()

        bpair = Pair(nameAsString, value)
    }

    public val bvalue:BType<out Any>
        get() = bpair.second

    public val name:String
        get() = bpair.first

    override val value: Pair<String, Any>
            get() = Pair(name, bpair.second.value)

    override var startPosition: Long = 0L
    override var endPosition: Long = 0L
}