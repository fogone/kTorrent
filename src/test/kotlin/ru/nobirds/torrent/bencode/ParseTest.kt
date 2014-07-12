package ru.nobirds.torrent.bencode

import org.junit.Test
import java.io.FileInputStream
import java.math.BigInteger
import java.io.FileOutputStream
import org.junit.Assert
import ru.nobirds.torrent.client.parser.TorrentParserImpl
import ru.nobirds.torrent.client.parser.Bencoder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import ru.nobirds.torrent.client.DigestProvider
import java.net.ServerSocket
import java.io.File
import java.net.URLEncoder
import java.net.URLDecoder
import ru.nobirds.torrent.client.parser.TorrentSerializer
import ru.nobirds.torrent.client.parser.OldTorrentParserImpl
import java.security.MessageDigest


public class BencodeTest() {

    val digestProvider = DigestProvider { MessageDigest.getInstance("SHA-1") }
    val parserService = TorrentParserImpl(digestProvider)

    Test
    public fun infoHashTest() {

        val source = ClassLoader.getSystemResourceAsStream("test2.torrent")!!.readBytes()

        val map = Bencoder.decodeBMap(ByteArrayInputStream(source))

        val torrent = parserService.parse(map)

        val infoHash = torrent.info.hash

        val info = BMapHelper(map).getBMap("info")!!

        val start = info.startPosition.toInt()
        val end = info.endPosition.toInt()

        val infoBytes = source.copyOfRange(start, end)
        val encoded = digestProvider.encode(infoBytes)

        Assert.assertArrayEquals(encoded, infoHash)
    }

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

    public fun test5() {
        val map = Bencoder.decodeBMap(FileInputStream("tmp.torrent"))
        val torrent = TorrentParserImpl(digestProvider).parse(map)
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
        val helper = BMapHelper(result)

        val warningMessage = helper.getString("warning message")

        Assert.assertEquals("Invalid info_hash", warningMessage)
    }

    Test
    public fun test6() {
        val parser = TorrentParserImpl(digestProvider)
        val serializer = TorrentSerializer()

        val source = ClassLoader.getSystemResourceAsStream("test1.torrent")!!
        val torrent = parser.parse(source)

        val buffer = ByteArrayOutputStream()
        serializer.serialize(torrent, buffer)

        val torrent2 = parser.parse(ByteArrayInputStream(buffer.toByteArray()))

        Assert.assertTrue(torrent.equals(torrent2))
    }

    Test
    public fun test7() {
        val parser = OldTorrentParserImpl(digestProvider)
        val parser2 = TorrentParserImpl(digestProvider)

        val torrent = parser.parse(ClassLoader.getSystemResourceAsStream("test1.torrent")!!)
        val torrent2 = parser2.parse(ClassLoader.getSystemResourceAsStream("test1.torrent")!!)

        Assert.assertTrue(torrent.equals(torrent2))
    }
}