package ru.nobirds.torrent.bencode

import io.netty.buffer.Unpooled
import org.junit.Assert
import org.junit.Test
import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.bencode.Bencoder
import ru.nobirds.torrent.parser.OldTorrentParserImpl
import ru.nobirds.torrent.parser.TorrentParserImpl
import ru.nobirds.torrent.parser.TorrentSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest


class BencodeTest() {

    val digestProvider = DigestProvider { MessageDigest.getInstance("SHA-1") }
    val parserService = TorrentParserImpl(digestProvider)

    @Test fun infoHashTest() {

        val source = ClassLoader.getSystemResourceAsStream("test2.torrent")!!.readBytes()

        val map = Bencoder.decodeBMap(ByteArrayInputStream(source))

        val torrent = parserService.parse(map)

        val infoHash = torrent.info.hash

        val info = map.getBMap("info")!!

        val start = info.startPosition.toInt()
        val end = info.endPosition.toInt()

        val infoBytes = source.copyOfRange(start, end)
        val encoded = digestProvider.encode(infoBytes)

        Assert.assertArrayEquals(encoded, infoHash)
    }

    @Test fun test3() {
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

    @Test fun encodeDecodeTest() {
        //val source = ClassLoader.getSystemResourceAsStream("tmp2.bencode")!!.readBytes()

        val bMap = BTypeFactory.createBMap {
            value("hello", "world")
            value("text", 10)
        }

        val source = Unpooled.buffer(500)
        val writer = BTokenBufferWriter(source)
        writer.write(bMap)
        writer.write(bMap)

        val buffer = source

        val stream = BTokenStreamImpl(BufferByteReader(buffer))

        stream.next()

        val bType = stream.processBType()

        println(bType)

        // val stream2 = BTokenStreamImpl(BufferByteReader(buffer))

        stream.next()

        val bType2 = stream.processBType()

        println(bType2)
    }

    @Test fun test5() {
        val map = Bencoder.decodeBMap(FileInputStream("tmp.torrent"))
        val torrent = TorrentParserImpl(digestProvider).parse(map)
        println(torrent)
    }

    @Test fun test4() {
        val source = ClassLoader.getSystemResourceAsStream("test1.torrent")!!.readBytes()

        val map = Bencoder.decodeBMap(ByteArrayInputStream(source))

        val target = Bencoder.encodeBType(map)

        Assert.assertArrayEquals(source, target)
    }

    @Test fun test2() {
        val stream = ClassLoader.getSystemResourceAsStream("tmp.bencode")!!
        val result = Bencoder.decodeBMap(stream)

        val warningMessage = result.getString("warning message")

        Assert.assertEquals("Invalid info_hash", warningMessage)
    }

    @Test fun test6() {
        val parser = TorrentParserImpl(digestProvider)
        val serializer = TorrentSerializer()

        val source = ClassLoader.getSystemResourceAsStream("test1.torrent")!!
        val torrent = parser.parse(source)

        val buffer = ByteArrayOutputStream()
        serializer.serialize(torrent, buffer)

        val torrent2 = parser.parse(ByteArrayInputStream(buffer.toByteArray()))

        Assert.assertTrue(torrent.equals(torrent2))
    }

    @Test
    fun test7() {
        val parser = OldTorrentParserImpl(digestProvider)
        val parser2 = TorrentParserImpl(digestProvider)

        val torrent = parser.parse(ClassLoader.getSystemResourceAsStream("test1.torrent")!!)
        val torrent2 = parser2.parse(ClassLoader.getSystemResourceAsStream("test1.torrent")!!)

        Assert.assertTrue(torrent.equals(torrent2))
    }
}