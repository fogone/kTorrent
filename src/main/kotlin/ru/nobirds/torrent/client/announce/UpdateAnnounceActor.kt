package ru.nobirds.torrent.client.announce

import org.springframework.web.client.RestTemplate
import java.net.URL
import org.springframework.web.util.UriComponentsBuilder
import ru.nobirds.torrent.client.Peer
import org.springframework.util.MultiValueMap
import java.net.InetSocketAddress
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.client.parser.BEncodeHttpMessageConverter
import ru.nobirds.torrent.bencode.BType
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.bencode.BMapHelper
import ru.nobirds.torrent.utils.multiValueMapOf
import ru.nobirds.torrent.utils.toInetSocketAddresses
import ru.nobirds.torrent.utils.toUrlString
import akka.actor.UntypedActor

public data class UpdateAnnounceMessage(
        val url:URL, val localPeer: Peer,
        val torrentHash: ByteArray,
        val size:Long, val uploaded:Long, val downloaded:Long)

public data class TrackerInfoMessage(
        val url:URL, val hash: ByteArray,
        val interval:Long, val peers:List<Peer>,
        val complete:Int, val incomplete:Int,
        val trackerId:String?, val warning:String?)

public data class TrackerUpdateFailedMessage(
        val url: URL, val hash: ByteArray, val message:String?)

public class UpdateAnnounceActor : UntypedActor() {

    private val template = RestTemplate(arrayListOf(BEncodeHttpMessageConverter()))

    override fun onReceive(message: Any?) {
        when(message) {
            is UpdateAnnounceMessage -> updateInfo(message)
        }
    }

    private fun updateInfo(message: UpdateAnnounceMessage) {
        try {
            sender()!!.tell(updateInfoImpl(message), self())
        } catch(e: Exception) {
            sender()!!.tell(TrackerUpdateFailedMessage(message.url, message.torrentHash, e.getMessage()), self())
        }
    }

    private fun updateInfoImpl(message: UpdateAnnounceMessage): TrackerInfoMessage {

        val parameters = createUrlParameters(message.localPeer, message.torrentHash, message.size, message.uploaded, message.downloaded)

        val uri = UriComponentsBuilder.fromUri(message.url.toURI())
                .queryParams(parameters)
                .build(true).toUri()

        val result = template.getForObject(uri, javaClass<BMap>())

        val helper = BMapHelper(result!!)

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

        val trackerInfo = TrackerInfoMessage(message.url, message.torrentHash, interval * 1000L, peers, complete, incomplete, trackerId, warning)

        return trackerInfo
    }

    private fun createUrlParameters(localPeer: Peer, torrentHash: ByteArray, size:Long, uploaded:Long, downloaded:Long):MultiValueMap<String, String> {
        return multiValueMapOf(
                "info_hash" to torrentHash.toUrlString(),
                "peer_id" to localPeer.id.toUrlString(),
                //"ip" to localPeer.address.getAddress().toString(),
                "port" to localPeer.address.getPort().toString(),
                "uploaded" to uploaded.toString(),
                "downloaded" to downloaded.toString(),
                "left" to (size - downloaded).toString()
        )
    }

    private fun fetchPeers(peers:BType):List<Peer> {
        return when(peers) {
            is BList -> parseFullPeersList(peers)
            is BBytes -> peers.value.toInetSocketAddresses().map { Peer(ByteArray(0), it) }
            else -> throw IllegalArgumentException()
        }
    }

    private fun parseFullPeersList(peers:BList):List<Peer> = peers.map {
        val peer = BMapHelper(it as BMap)
        Peer(peer.getBytes("id")!!, InetSocketAddress(peer.getString("ip")!!, peer.getLong("port")!!.toInt()))
    }

}