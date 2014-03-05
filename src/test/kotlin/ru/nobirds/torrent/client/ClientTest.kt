package ru.nobirds.torrent.client

import org.junit.Test
import ru.nobirds.torrent.client.task.TorrentTaskManager
import ru.nobirds.torrent.config.Config
import java.util.HashMap
import java.util.Properties
import ru.nobirds.torrent.config.Configs
import org.springframework.web.client.RestTemplate
import ru.nobirds.torrent.client.parser.BEncodeHttpMessageConverter
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.client.parser.MapHelper
import org.junit.Assert
import ru.nobirds.torrent.client.parser.TorrentParser
import ru.nobirds.torrent.client.parser.Bencoder
import java.io.ByteArrayInputStream
import java.net.URLEncoder

public class ClientTest {

    Test
    public fun test1() {
        val config = Configs.fromProperties("client.properties")

        val taskManager = TorrentTaskManager(config)

        taskManager.add(ClassLoader.getSystemResourceAsStream("test1.torrent")!!)

    }

    Test
    public fun announceTest() {
        val restTemplate = RestTemplate(arrayListOf(BEncodeHttpMessageConverter()))

        val torrent = TorrentParser().parse(ClassLoader.getSystemResourceAsStream("test3.torrent")!!)

        val url = "${torrent.announce.url.toString()}&info_hash={hash}&peer_id=67e949410121c9c2fd4791b357d29e2ec16d71ce&port=6881"

        val result = restTemplate.getForObject(url, javaClass<BMap>(), hashMapOf("hash" to Utils.byteArrayToURLString(torrent.info.hash)))!!
//        val result = restTemplate.getForObject(url, javaClass<BMap>(), hashMapOf("hash" to URLEncoder.encode(torrent.info.hash.toString(), "UTF-8")))!!

        val map = MapHelper(result)

        Assert.assertEquals("Invalid info_hash", map.getString("warning message"))
    }
}