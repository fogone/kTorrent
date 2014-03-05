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


public class BencodeTest() {

    val parserService = TorrentParser()

    Test
    public fun infoHashTest() {

        val stream = ClassLoader.getSystemResourceAsStream("test1.torrent")!!

        val byteArrayOutputStream = ByteArrayOutputStream()
        stream.buffered().copyTo(byteArrayOutputStream)

        val source = byteArrayOutputStream.toByteArray()

        val map = Bencoder.decodeBMap(ByteArrayInputStream(source))

        val torrent = parserService.parse(map)

        val infoHash = torrent.info.hash

        val binfo = map["info"]!!

        val infoBytes = source.copyOfRange(binfo.startPosition.toInt(), binfo.endPosition.toInt())

        val encoded = Sha1Provider.encode(infoBytes)

        Assert.assertEquals(encoded, infoHash)
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