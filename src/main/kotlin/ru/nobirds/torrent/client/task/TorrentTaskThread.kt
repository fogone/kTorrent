package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.model.Torrent

public class TorrentTaskThread(val torrent:Torrent) : Thread("Torrent Task") {

    override fun run() {
        val task = TorrentTask(torrent)
        while(!isInterrupted()) {

        }
    }

}

