package ru.nobirds.torrent.client

import org.junit.Test
import ru.nobirds.torrent.client.task.TorrentTaskManagerActor
import ru.nobirds.torrent.config.Config
import java.util.HashMap
import java.util.Properties
import ru.nobirds.torrent.config.Configs
import org.springframework.web.client.RestTemplate
import ru.nobirds.torrent.client.parser.BEncodeHttpMessageConverter
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.client.parser.MapHelper
import org.junit.Assert
import ru.nobirds.torrent.client.parser.TorrentParserImpl
import ru.nobirds.torrent.client.parser.Bencoder
import java.io.ByteArrayInputStream
import java.net.URLEncoder
import ru.nobirds.torrent.asString
import ru.nobirds.torrent.toUrlString

public class ClientTest {

    Test
    public fun test1() {

        val client = Client()

        client.taskManager.add(ClassLoader.getSystemResourceAsStream("test4.torrent")!!)
        client.start()
    }

    Test
    public fun announceTest() {
        val restTemplate = RestTemplate(arrayListOf(BEncodeHttpMessageConverter()))

        val torrent = TorrentParserImpl().parse(ClassLoader.getSystemResourceAsStream("test2.torrent")!!)

        val url = "http://comoros.ti.ru/announce?info_hash=${torrent.info.hash!!.toUrlString()}&peer_id=&port=6881"

        val result = restTemplate.getForObject(url, javaClass<BMap>())!!
//        val result = restTemplate.getForObject(url, javaClass<BMap>(), hashMapOf("hash" to URLEncoder.encode(torrent.info.hash.toString(), "UTF-8")))!!

        val map = MapHelper(result)

        Assert.assertEquals("Invalid info_hash", map.getString("warning message"))
    }
}