package ru.nobirds.torrent.announce

import org.springframework.http.converter.HttpMessageConverter
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BType
import ru.nobirds.torrent.parser.BEncodeHttpMessageConverter
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.toId
import ru.nobirds.torrent.utils.toInetSocketAddresses
import ru.nobirds.torrent.utils.toUrlString
import java.net.InetSocketAddress
import java.net.URI

public class HttpAnnounceProvider : AnnounceProvider {

    private val template = RestTemplate(arrayListOf<HttpMessageConverter<*>>(BEncodeHttpMessageConverter()))

    public override fun getTrackerInfoByUrl(uri: URI, localPeer: Peer, hash: Id): TrackerInfo {
        val parameters = createUrlParameters(localPeer, hash)

        val queryUri = UriComponentsBuilder.fromUri(uri)
                .queryParams(parameters)
                .build(true).toUri()

        val result = template.getForObject(queryUri, BMap::class.java)

        val failureReason = result.getString("failure reason")

        processFailure(failureReason)

        val warning = result.getString("warning message")

        processWarning(warning)

        val interval = result.getLong("interval")!!

        val peers = fetchPeers(hash, result.getValue("peers")!!)

        val complete = result.getInt("complete")!!

        val incomplete = result.getInt("incomplete")!!

        val trackerId = result.getString("tracker id")

        return TrackerInfo(interval * 1000L, peers, complete, incomplete, trackerId, warning)
    }

    private fun processFailure(failureReason: String?) {
        if (failureReason != null)
            throw TrackerRequestException(failureReason)
    }

    private fun processWarning(warning: String?) {
        if (warning != null && warning.equals("Invalid info_hash", ignoreCase = true))
            throw InfoHashNotFoundException()
    }

    private fun createUrlParameters(localPeer: Peer, hash: Id): MultiValueMap<String, String> {
        return utils.multiValueMapOf(
                "info_hash" to hash.toBytes().toUrlString(),
                "peer_id" to localPeer.id.toBytes().toUrlString(),
                //"ip" to localPeer.address.getAddress().toString(),
                "port" to localPeer.address.getPort().toString(),
                "uploaded" to "0",
                "downloaded" to "0",
                "left" to "0"
        )
    }

    private fun fetchPeers(hash: Id, peers: BType):List<Peer> {
        return when(peers) {
            is BList -> parseFullPeersList(hash, peers)
            is BBytes -> peers.value.toInetSocketAddresses().map { Peer(hash, Id.Zero, it) }
            else -> throw IllegalArgumentException()
        }
    }

    private fun parseFullPeersList(hash: Id, peers: BList):List<Peer> = peers.map {
        val peer = it as BMap
        Peer(hash, peer.getBytes("id")!!.toId(), InetSocketAddress(peer.getString("ip")!!, peer.getLong("port")!!.toInt()))
    }

}