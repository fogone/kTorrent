package ru.nobirds.torrent.bencode

public trait BValueType<T> : BType {

    val value:T

}