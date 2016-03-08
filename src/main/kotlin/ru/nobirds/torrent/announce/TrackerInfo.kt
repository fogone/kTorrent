package ru.nobirds.torrent.announce

import ru.nobirds.torrent.peers.Peer

data class TrackerInfo(
        val interval:Long, val peers:List<Peer>,
        val complete:Int, val incomplete:Int,
        val trackerId:String?, val warning:String?)

