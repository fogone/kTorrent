package ru.nobirds.torrent.bencode

public trait BType {

    fun process(stream: BTokenInputStream)

    val startPosition:Long

    val endPosition:Long

}