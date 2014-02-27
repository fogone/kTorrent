package ru.nobirds.torrent.bencode

import org.junit.Test
import java.io.FileInputStream
import java.math.BigInteger
import ru.nobirds.torrent.TorrentParser

public class BencodeTest() {

    Test
    public fun test1() {

        val torrent = TorrentParser().parse(ClassLoader.getSystemResourceAsStream("test1.torrent")!!)

        println(torrent)
    }

}