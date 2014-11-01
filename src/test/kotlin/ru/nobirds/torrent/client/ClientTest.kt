package ru.nobirds.torrent.client

import org.junit.Test
import org.springframework.web.client.RestTemplate
import ru.nobirds.torrent.parser.BEncodeHttpMessageConverter
import ru.nobirds.torrent.bencode.BMap
import org.junit.Assert
import ru.nobirds.torrent.parser.TorrentParserImpl
import ru.nobirds.torrent.utils.toUrlString
import java.security.MessageDigest

public class ClientTest {

    Test
    public fun announceTest() {
        val restTemplate = RestTemplate(arrayListOf(BEncodeHttpMessageConverter()))

        val torrent = TorrentParserImpl(DigestProvider { MessageDigest.getInstance("SHA-1") }).parse(ClassLoader.getSystemResourceAsStream("test2.torrent")!!)

        val url = "http://comoros.ti.ru/announce?info_hash=${torrent.info.hash!!.toUrlString()}&peer_id=&port=6881"

        val result = restTemplate.getForObject(url, javaClass<BMap>())!!
//        val result = restTemplate.getForObject(url, javaClass<BMap>(), hashMapOf("hash" to URLEncoder.encode(torrent.info.hash.toString(), "UTF-8")))!!

        val map = result

        Assert.assertEquals("Invalid info_hash", map.getString("warning message"))
    }
}