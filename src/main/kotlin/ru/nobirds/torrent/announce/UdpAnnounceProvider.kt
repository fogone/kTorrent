package ru.nobirds.torrent.announce

import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.Id
import java.net.URI

public class UdpAnnounceProvider : AnnounceProvider {

    override fun getTrackerInfoByUrl(uri: URI, localPeer: Peer, hash: Id): TrackerInfo {
        throw UnsupportedOperationException("Udp not implemented yet.")
    }

}