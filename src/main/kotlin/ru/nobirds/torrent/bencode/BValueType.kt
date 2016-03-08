package ru.nobirds.torrent.bencode

interface BValueType<T> : BType {

    val value:T

}