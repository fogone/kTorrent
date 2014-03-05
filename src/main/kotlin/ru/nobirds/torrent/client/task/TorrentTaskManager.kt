package ru.nobirds.torrent.client.task

import java.util.ArrayList
import ru.nobirds.torrent.client.model.Torrent


import ru.nobirds.torrent.client.parser.TorrentParser
import java.io.InputStream
import ru.nobirds.torrent.config.Config
import ru.nobirds.torrent.client.ClientProperties
import ru.nobirds.torrent.client.LocalPeerFactory
import ru.nobirds.torrent.client.announce.AnnounceService

public class TorrentTaskManager(val config:Config) {

    private val parserService: TorrentParser = TorrentParser()

    private val announceService:AnnounceService = AnnounceService()

    private val tasks = ArrayList<TorrentTask>()

    public fun add(torrent:InputStream) {
        add(parserService.parse(torrent))
    }

    public fun add(torrent:Torrent) {
        val directory = config.get(ClientProperties.torrentsDirectory)

        val task = TorrentTask(
                LocalPeerFactory.createLocalPeer(config.get(ClientProperties.clientPortsRange)),
                directory, torrent)

        tasks.add(task)
        announceService.registerTask(task)
        task.start()
    }

}