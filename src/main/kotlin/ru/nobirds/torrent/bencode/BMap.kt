package ru.nobirds.torrent.bencode

import java.util.LinkedHashMap
import ru.nobirds.torrent.nullOr
import java.io.StringWriter


public class BMap(
        private val children:MutableMap<String, BKeyValuePair> = LinkedHashMap()
) : AbstractBlockBType('d'), MutableMap<String, BKeyValuePair> by children {

    public fun getValue(name:String):BType? = get(name).nullOr { value }

    public fun putValue(name:String, value:BType) {
        put(name, BKeyValuePair().set(name, value))
    }

    public fun getOrPutValue(name:String, defaultValue:()->BType):BType
            = getOrPut(name) { BKeyValuePair().set(name, defaultValue()) }.value

    override fun onChar(stream: BTokenInputStream) {
        val bpair = BKeyValuePair()
        bpair.process(stream)
        children.put(bpair.name, bpair)
    }

}