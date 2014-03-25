package ru.nobirds.torrent.kademlia

import ru.nobirds.torrent.fillWith
import ru.nobirds.torrent.xor

public data class Id(factory:(Int)->Byte) {

    public val size:Int = 16

    private val bytes = ByteArray(size).fillWith(factory)

    fun xor(k:Id):Id = Id { bytes[it] xor k.bytes[it] }

}