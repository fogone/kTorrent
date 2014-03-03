package ru.nobirds.torrent.client.task

import java.util.ArrayList
import ru.nobirds.torrent.client.model.Torrent
import org.springframework.stereotype.Service as service
import org.springframework.beans.factory.annotation.Autowired as autowired
import ru.nobirds.torrent.client.parser.TorrentParserService
import java.io.InputStream
import ru.nobirds.torrent.config.Config
import ru.nobirds.torrent.client.ClientProperties
import ru.nobirds.torrent.client.LocalPeerService
import ru.nobirds.torrent.client.announce.AnnounceService

public service class TorrentTaskManager() {

    private autowired var parserService:TorrentParserService? = null
    private autowired var localPeerService:LocalPeerService? = null
    private autowired var announceService:AnnounceService? = null
    private autowired var config:Config? = null

    private val tasks = ArrayList<TorrentTask>()

    public fun add(torrent:InputStream) {
        add(parserService!!.parse(torrent))
    }

    public fun add(torrent:Torrent) {
        val directory = config!!.get(ClientProperties.torrentsDirectory)
        val task = TorrentTask(localPeerService!!.createLocalPeer(), directory, torrent)
        tasks.add(task)
        announceService!!.registerTask(task)
        task.start()
    }

}