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
import ru.nobirds.torrent.asString
import ru.nobirds.torrent.toUrlString
import ru.nobirds.torrent.bencode.BType
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BBytes
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.net.InetAddress
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.nio.ByteOrder

public class AnnounceService {

    private val template = RestTemplate(arrayListOf(BEncodeHttpMessageConverter()))

    public fun getTrackerInfoByUrl(task:TorrentTask, url:URL): TrackerInfo {
        val parameters = createUrlParameters(task)

        val uri = UriComponentsBuilder.fromUri(url.toURI())
                .queryParams(parameters)
                .build(true).toUri()

        val result = template.getForObject(uri, javaClass<BMap>())

        val helper = MapHelper(result!!)

        val failureReason = helper.getString("failure reason")

        if(failureReason != null)
            throw TrackerRequestException(failureReason)

        val warning = helper.getString("warning message")

        if(warning != null && warning.equalsIgnoreCase("Invalid info_hash"))
            throw InfoHashNotFoundException()

        val interval = helper.getLong("interval")!!

        val peers = fetchPeers(helper.map.getValue("peers")!!)

        val complete = helper.getInt("complete")!!

        val incomplete = helper.getInt("incomplete")!!

        val trackerId = helper.getString("tracker id")

        return TrackerInfo(interval * 1000L, peers, complete, incomplete, trackerId, warning)
    }

    private fun createUrlParameters(task:TorrentTask):MultiValueMap<String, String> {
        val localPeer = task.peer

        val torrent = task.torrent.info

        val uploaded = task.uploadStatistics.totalInBytes
        val downloaded = task.downloadStatistics.totalInBytes

        val total = torrent.files.totalLength

        return multiValueMapOf(
                "info_hash" to torrent.hash.toUrlString(),
                "peer_id" to localPeer.id.toUrlString(),
                //"ip" to localPeer.address.getAddress().toString(),
                "port" to localPeer.address.getPort().toString(),
                "uploaded" to uploaded.toString(),
                "downloaded" to downloaded.toString(),
                "left" to (total - downloaded).toString()
        )
    }

    private fun fetchPeers(peers:BType):List<Peer> {
        return when(peers) {
            is BList -> parseFullPeersList(peers)
            is BBytes -> parseCompactPeersList(peers.value)
            else -> throw IllegalArgumentException()
        }
    }

    private fun parseFullPeersList(peers:BList):List<Peer> = peers.map {
        val peer = MapHelper(it as BMap)
        Peer(peer.getBytes("id")!!, InetSocketAddress(peer.getString("ip")!!, peer.getLong("port")!!.toInt()))
    }

    private fun parseCompactPeersList(bytes:ByteArray):List<Peer> {
        val source = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)

        val ip = ByteArray(4)
        return (0..bytes.size / 6 - 1).map {
            source.get(ip)
            val port = source.getShort().toInt() and 0xffff
            Peer(ByteArray(0), InetSocketAddress(InetAddress.getByAddress(ip), port))
        }
    }
}