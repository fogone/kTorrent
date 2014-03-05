package ru.nobirds.torrent.bencode

import java.util.ArrayList

public class BList() : AbstractBlockBType<List<Any>>('l'), Iterable<BType<out Any>> {

    private val list:MutableList<Any> = ArrayList()

    private val children = ArrayList<BType<out Any>>()

    public override fun iterator():Iterator<BType<out Any>>
            = children.iterator()

    override fun onChar(stream: BTokenInputStream) {
        val bvalue = stream.processBType()
        children.add(bvalue)
        list.add(bvalue.value)
    }

    override fun createResult(): List<Any> = list
}