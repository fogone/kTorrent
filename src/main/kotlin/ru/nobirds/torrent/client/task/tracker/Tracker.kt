package ru.nobirds.torrent.client.task.tracker

import ru.nobirds.torrent.client.Peer

public trait Tracker {

    val status:TrackerStatus

    fun registerUpdateListener(listener:(peers:List<Peer>)->Unit)

}