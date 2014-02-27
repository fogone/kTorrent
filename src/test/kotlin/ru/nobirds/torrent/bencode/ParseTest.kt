package ru.nobirds.torrent.bencode

import org.junit.Test
import java.io.FileInputStream
import java.math.BigInteger
import ru.nobirds.torrent.TorrentParser
import java.io.FileOutputStream
import org.junit.Assert

public class BencodeTest() {

    Test
    public fun test1() {

        val torrent = TorrentParser().parse(ClassLoader.getSystemResourceAsStream("test1.torrent")!!)

        println(torrent)
    }

    Test
    public fun writeTest() {

        val map = Bencoder.decode(ClassLoader.getSystemResourceAsStream("test1.torrent")!!)

        val original = TorrentParser().parse(map)

        Bencoder.encode(FileOutputStream("result.torrent"), map)

        val torrent = TorrentParser().parse(FileInputStream("result.torrent"))

        Assert.assertTrue(original.equals(torrent))
    }

}