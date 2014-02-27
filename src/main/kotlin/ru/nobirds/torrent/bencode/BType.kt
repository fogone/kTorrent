package ru.nobirds.torrent.bencode

public trait BType<T> {

    fun process(stream: BTokenInputStream)

    val value:T

}