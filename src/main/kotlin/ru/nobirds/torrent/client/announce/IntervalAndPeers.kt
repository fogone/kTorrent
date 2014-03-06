package ru.nobirds.torrent.client.announce

import ru.nobirds.torrent.client.Peer

public data class IntervalAndPeers(val interval:Long, val peers:List<Peer>)

