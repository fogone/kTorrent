package ru.nobirds.torrent.bencode

import java.util.HashMap


public class BMap() : AbstractBlockBType<Map<String, Any>>('d') {

    val map:MutableMap<String, Any> = HashMap()

    override fun onChar(stream: BTokenInputStream) {
        val name = stream.processBType() as BBytes

        stream.next()

        val value = stream.processBType()

        map.put(name.toString(), value.value)
    }

    override fun createResult(): Map<String, Any> = map

}