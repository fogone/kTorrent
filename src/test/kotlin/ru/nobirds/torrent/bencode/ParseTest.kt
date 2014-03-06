package ru.nobirds.torrent.bencode

import org.junit.Test
import java.io.FileInputStream
import java.math.BigInteger
import java.io.FileOutputStream
import org.junit.Assert
import ru.nobirds.torrent.client.parser.TorrentParser
import ru.nobirds.torrent.client.parser.Bencoder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import ru.nobirds.torrent.client.Sha1Provider
import ru.nobirds.torrent.client.parser.MapHelper
import ru.nobirds.torrent.asString
import java.net.ServerSocket
import java.io.File
import java.net.URLEncoder
import java.net.URLDecoder


public class BencodeTest() {

    val parserService = TorrentParser()

    Test
    public fun infoHashTest() {

        val source = ClassLoader.getSystemResourceAsStream("test2.torrent")!!.readBytes()

        val map = Bencoder.decodeBMap(ByteArrayInputStream(source))

        val torrent = parserService.parse(map)

        val infoHash = torrent.info.hash

        val info = MapHelper(map).getBMap("info")!!

        val start = info.startPosition.toInt()
        val end = info.endPosition.toInt()

        val infoBytes = source.copyOfRange(start, end)
        val encoded = Sha1Provider.encodeAsBytes(infoBytes)

        Assert.assertArrayEquals(encoded, infoHash)
    }

    Test
    public fun test3() {
        val source = ClassLoader.getSystemResourceAsStream("test2.torrent")!!.readBytes()

        val map = Bencoder.decodeBMap(ByteArrayInputStream(source))

        BTypeFactory.createBMap(map) {
            value("announce", "http://comoros.ti.ru/announce")
            list("announce-list") {
                clear()
            }
        }

        Bencoder.encodeBType(FileOutputStream("tmp.torrent"), map)
    }

    Test
    public fun test5() {
        val map = Bencoder.decodeBMap(FileInputStream("tmp.torrent"))
        val torrent = TorrentParser().parse(map)
        println(torrent)
    }

    Test
    public fun test4() {
        val source = ClassLoader.getSystemResourceAsStream("test1.torrent")!!.readBytes()

        val map = Bencoder.decodeBMap(ByteArrayInputStream(source))

        val target = Bencoder.encodeBType(map)

        Assert.assertArrayEquals(source, target)
    }

    Test
    public fun test2() {
        val stream = ClassLoader.getSystemResourceAsStream("tmp.bencode")!!
        val result = Bencoder.decodeBMap(stream)
        val helper = MapHelper(result)

        val warningMessage = helper.getString("warning message")

        Assert.assertEquals("Invalid info_hash", warningMessage)
    }

}