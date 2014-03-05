package ru.nobirds.torrent.client.announce

import org.springframework.web.client.RestTemplate
import java.net.URL
import org.springframework.web.util.UriComponentsBuilder
import ru.nobirds.torrent.client.task.TorrentTask
import ru.nobirds.torrent.client.parser.MapHelper
import ru.nobirds.torrent.toHexString
import ru.nobirds.torrent.client.Peer
import ru.nobirds.torrent.multiValueMapOf
import org.springframework.util.MultiValueMap
import java.util.Collections
import java.net.InetSocketAddress
import java.util.HashMap
import java.util.Timer
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.client.parser.BEncodeHttpMessageConverter

public class AnnounceService {

    private val template = RestTemplate(arrayListOf(BEncodeHttpMessageConverter()))

    private val timer = Timer()

    private val announces = HashMap<URL, AnnounceUpdater>()

    public fun registerAnnounce(url: URL, updateInterval: Long) {
        announces.getOrPut(url) { AnnounceUpdater(this, url, timer, updateInterval) }.renewInterval(updateInterval)
    }

    public fun registerTask(task:TorrentTask) {
        task.torrent.announce.allUrls.forEach {
            announces.getOrPut(it) { AnnounceUpdater(this, it, timer) }.registerTask(task)
        }
    }

    public fun getPeersForTask(task:TorrentTask, url:URL):List<Peer> {
        try {
            val parameters = createUrlParameters(task)

            val uri = UriComponentsBuilder.fromUri(url.toURI())
                    .queryParams(parameters)
                    .build().toUri()

            val result = template.getForObject(uri, javaClass<BMap>())

            val helper = MapHelper(result!!)

            val warning = helper.getString("warning message")

            if(warning != null && warning.equalsIgnoreCase("Invalid info_hash"))
                throw TorrentNotFoundException(task.torrent)

            val interval = helper.getLong("interval")!!

            registerAnnounce(url, interval)

            return fetchPeers(helper.getListOfMaps("peers")!!)
        } catch(e: Exception) {
            e.printStackTrace()
            return Collections.emptyList()
        }
    }

    private fun createUrlParameters(task:TorrentTask):MultiValueMap<String, String> {
        val localPeer = task.peer

        val torrent = task.torrent.info

        val uploaded = task.uploadStatistics.totalInBytes
        val downloaded = task.downloadStatistics.totalInBytes

        val total = torrent.files.totalLength

        return multiValueMapOf(
                "info_hash" to torrent.hash,
                "peer_id" to localPeer.id,
                //"ip" to localPeer.address.getAddress().toString(),
                "port" to localPeer.address.getPort().toString(),
                "uploaded" to uploaded.toString(),
                "downloaded" to downloaded.toString(),
                "left" to (total - downloaded).toString()
        )
    }

    private fun fetchPeers(result:List<MapHelper>):List<Peer> {
        return result.map {
            Peer(it.getBytes("id")!!.toHexString(),
                    InetSocketAddress(it.getString("ip")!!, it.getLong("port")!!.toInt()))
        }
    }

}