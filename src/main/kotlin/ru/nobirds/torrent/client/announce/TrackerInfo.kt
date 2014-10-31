package ru.nobirds.torrent.client.announce

import ru.nobirds.torrent.client.Peer

public data class TrackerInfo(
        val interval:Long, val peers:List<Peer>,
        val complete:Int, val incomplete:Int,
        val trackerId:String?, val warning:String?)

