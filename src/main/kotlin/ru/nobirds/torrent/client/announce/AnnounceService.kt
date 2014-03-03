package ru.nobirds.torrent.client.announce

import org.springframework.web.client.RestTemplate
import java.net.URL
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.beans.factory.annotation.Autowired as autowired
import org.springframework.stereotype.Service as service
import ru.nobirds.torrent.client.task.TorrentTask
import ru.nobirds.torrent.client.parser.MapHelper
import ru.nobirds.torrent.toHexString
import ru.nobirds.torrent.client.BEncodeHttpMessageConverter
import ru.nobirds.torrent.client.Peer
import ru.nobirds.torrent.multiValueMapOf
import org.springframework.util.MultiValueMap
import java.util.concurrent.ConcurrentHashMap
import java.util.ArrayList
import java.util.Collections

public service class AnnounceService : AnnounceListener {

    private autowired var announceUpdaterService:AnnounceUpdaterService? = null

    private val tasksByUrls = ConcurrentHashMap<URL, MutableList<TorrentTask>>()

    private val template = RestTemplate(arrayListOf(BEncodeHttpMessageConverter()))

    public fun registerTask(task:TorrentTask) {
        task.torrent.announce.allUrls.forEach {
            tasksByUrls.getOrPut(it) { ArrayList() }.add(task)
            announceUpdaterService!!.registerListener(it, this)
        }
    }

    override fun onSchedule(url: URL) {
        tasksByUrls
                .getOrElse(url) { Collections.emptyList<TorrentTask>() }
                .forEach {
            updatePeers(url, it)
        }
    }

    private fun updatePeers(url:URL, task:TorrentTask) {
        task.updatePeers(url, processUrl(url, createUrlParameters(task)))
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
                "ip" to localPeer.ip,
                "port" to localPeer.port.toString(),
                "uploaded" to uploaded.toString(),
                "downloaded" to downloaded.toString(),
                "left" to (total - downloaded).toString()
        )
    }

    private fun processUrl(url: URL, parameters:MultiValueMap<String, String>):List<Peer> {
        try {
            val uri = UriComponentsBuilder.fromUri(url.toURI())
                    .queryParams(parameters)
                    .build().toUri()

            val result = template.getForObject(uri, javaClass<Map<String, Any>>())

            val helper = MapHelper(result!!)

            val interval = helper.getLong("interval")!!

            announceUpdaterService!!.registerAnnounce(url, interval)

            return fetchPeers(helper.getListOfMaps("peers")!!)
        } catch(e: Exception) {
            return Collections.emptyList()
        }
    }

    private fun fetchPeers(result:List<MapHelper>):List<Peer> {
        return result.map {
            Peer(
                    it.getBytes("id")!!.toHexString(),
                    it.getString("ip")!!,
                    it.getLong("port")!!.toInt())
        }
    }

}