package ru.nobirds.torrent.client.announce

import org.springframework.web.client.RestTemplate
import java.util.ArrayList
import java.net.URL
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.beans.factory.annotation.Autowired as autowired
import org.springframework.stereotype.Service as service
import ru.nobirds.torrent.client.task.TorrentTask
import org.springframework.web.util.UriComponents
import ru.nobirds.torrent.client.parser.MapHelper
import ru.nobirds.torrent.toHexString
import java.util.HashMap
import ru.nobirds.torrent.client.LocalPeerService
import ru.nobirds.torrent.client.BEncodeHttpMessageConverter
import ru.nobirds.torrent.client.Peer
import java.util.Collections

public service class AnnounceService : AnnounceListener {

    private autowired var announceUpdaterService:AnnounceUpdaterService? = null

    private autowired var localPeerService: LocalPeerService? = null

    private val tasksByUrls = HashMap<URL, MutableList<TorrentTask>>()

    private val template = RestTemplate(arrayListOf(BEncodeHttpMessageConverter()))

    public fun registerTask(task:TorrentTask) {
        task.torrent.announce.allUrls.forEach {
            announceUpdaterService!!.registerListener(it, this)
            tasksByUrls.getOrPut(it) { ArrayList() }.add(task)
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

    private fun createUrlParameters(task:TorrentTask):UriComponents {
        val localPeer = localPeerService!!.localPeer

        val torrent = task.torrent.info

        val uploaded = task.uploadStatistics.totalInBytes
        val downloaded = task.downloadStatistics.totalInBytes

        val total = torrent.files.totalLength

        return UriComponentsBuilder.newInstance()
                .queryParam("info_hash", torrent.hash)
                .queryParam("peer_id", localPeer.id)
                .queryParam("ip", localPeer.ip)
                .queryParam("port", localPeer.port)
                .queryParam("uploaded", uploaded)
                .queryParam("downloaded", downloaded)
                .queryParam("left", total - downloaded)
                .build()
    }

    private fun processUrl(url: URL, parameters:UriComponents):List<Peer> {

        val uri = UriComponentsBuilder.fromUri(url.toURI())
                .uriComponents(parameters)
                .build().toUri()

        val result = template.getForObject(uri, javaClass<Map<String, Any>>())

        val helper = MapHelper(result!!)

        val interval = helper.getLong("interval")!!

        announceUpdaterService!!.registerAnnounce(url, interval)

        return fetchPeers(helper.getListOfMaps("peers")!!)
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