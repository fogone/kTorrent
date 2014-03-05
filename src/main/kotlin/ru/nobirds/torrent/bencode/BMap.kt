package ru.nobirds.torrent.bencode

import java.util.LinkedHashMap


public class BMap() : AbstractBlockBType<Map<String, Any>>('d') {

    private val map:MutableMap<String, Any> = LinkedHashMap()

    private val children = LinkedHashMap<String, BKeyValuePair>()

    public val pairs:Iterable<BKeyValuePair>
        get() = children.values()

    public fun get(name:String):BKeyValuePair? = children[name]

    override fun onChar(stream: BTokenInputStream) {
        val bpair = BKeyValuePair()
        bpair.process(stream)

        val value = bpair.value

        children.put(bpair.name, bpair)
        map.put(value.first, value.second)
    }

    override fun createResult(): Map<String, Any> = map

}