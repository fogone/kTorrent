package ru.nobirds.torrent.client.announce

import org.springframework.stereotype.Service as service
import java.net.URL
import java.util.HashMap
import java.util.Timer
import java.util.TimerTask
import org.springframework.beans.factory.annotation.Autowired as autowired
import ru.nobirds.torrent.client.Peer
import java.util.ArrayList
import ru.nobirds.torrent.client.task.TorrentTask

public service class AnnounceUpdaterService {

    private val timer = Timer()

    private val announces = HashMap<URL, AnnounceMeta>()

    public fun registerAnnounce(url: URL, updateInterval: Long) {
        announces.getOrPut(url) { AnnounceMeta(url, timer, updateInterval) }.renewInterval(updateInterval)
    }

    public fun registerListener(url:URL, listener: AnnounceListener) {
        announces.getOrPut(url) { AnnounceMeta(url, timer, 0) }.registerListener(listener)
    }

}

