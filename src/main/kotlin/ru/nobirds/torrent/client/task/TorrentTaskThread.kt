package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.model.Torrent
import java.io.File

public class TorrentTaskThread(val directory:File, val torrent:Torrent) : Thread("Torrent Task") {

    override fun run() {
        val task = TorrentTask(directory, torrent)
        while(!isInterrupted()) {

        }
    }

}

