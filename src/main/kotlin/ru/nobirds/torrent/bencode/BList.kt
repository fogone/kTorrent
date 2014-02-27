package ru.nobirds.torrent.bencode

import java.util.ArrayList

public class BList() : AbstractBlockBType<List<Any>>('l') {

    private val list:MutableList<Any> = ArrayList()

    override fun onChar(stream: BTokenInputStream) {
        val value = stream.processBType().value
        list.add(value)
    }

    override fun createResult(): List<Any> = list
}