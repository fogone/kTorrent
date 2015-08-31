package ru.nobirds.torrent.bencode

public interface BValueType<T> : BType {

    val value:T

}