package ru.nobirds.torrent.client.task

import java.util.ArrayList
import ru.nobirds.torrent.client.model.Torrent
import org.springframework.stereotype.Service as service

public service class TorrentTaskManager() {

    private val tasks = ArrayList<TorrentTaskThread>()

    public fun add(torrent:Torrent) {
        val taskThread = TorrentTaskThread(torrent)
        tasks.add(taskThread)
        taskThread.start()
    }

}