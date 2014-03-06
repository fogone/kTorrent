package ru.nobirds.torrent.bencode

import java.util.ArrayList

public class BList(
        private val children:MutableList<BType> = ArrayList()
) : AbstractBlockBType('l'), MutableList<BType> by children {

    override fun onChar(stream: BTokenInputStream) {
        children.add(stream.processBType())
    }

}