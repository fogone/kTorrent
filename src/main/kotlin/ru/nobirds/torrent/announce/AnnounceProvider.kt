package ru.nobirds.torrent.announce

import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.Id
import java.net.URI

interface AnnounceProvider {

    fun getTrackerInfoByUrl(uri:URI, localPeer: Peer, hash: Id): TrackerInfo

}

