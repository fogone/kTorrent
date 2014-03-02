package ru.nobirds.torrent.client.task

import java.util.ArrayList
import ru.nobirds.torrent.client.model.Torrent
import org.springframework.stereotype.Service as service
import org.springframework.beans.factory.annotation.Autowired as autowired
import ru.nobirds.torrent.client.parser.TorrentParserService
import java.io.InputStream
import ru.nobirds.torrent.config.Config
import ru.nobirds.torrent.client.ClientProperties

public service class TorrentTaskManager() {

    private autowired var parserService:TorrentParserService? = null
    private autowired var config:Config? = null

    private val tasks = ArrayList<TorrentTaskThread>()

    public fun add(torrent:InputStream) {
        add(parserService!!.parse(torrent))
    }

    public fun add(torrent:Torrent) {
        val directory = config!!.get(ClientProperties.torrentsDirectory)
        val taskThread = TorrentTaskThread(directory, torrent)
        tasks.add(taskThread)
        taskThread.start()
    }

}