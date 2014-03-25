package ru.nobirds.torrent.client.task.tracker

import ru.nobirds.torrent.client.Peer


public class DhtTracker(val hash:ByteArray) : Tracker {

    override val status: TrackerStatus = TrackerStatus.working


    override fun registerUpdateListener(listener: (List<Peer>) -> Unit) {

    }


}