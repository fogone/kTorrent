package ru.nobirds.torrent.announce

import java.net.URI
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.Id

public class UdpAnnounceProvider : AnnounceProvider {

    override fun getTrackerInfoByUrl(uri: URI, localPeer: Peer, hash: Id): TrackerInfo {
        throw UnsupportedOperationException("Udp not implemented yet.")
    }

}