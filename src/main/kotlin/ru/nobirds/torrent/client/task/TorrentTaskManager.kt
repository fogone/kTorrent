package ru.nobirds.torrent.client.task

import java.util.ArrayList
import ru.nobirds.torrent.client.model.Torrent


import ru.nobirds.torrent.client.parser.TorrentParserImpl
import java.io.InputStream
import ru.nobirds.torrent.config.Config
import ru.nobirds.torrent.client.ClientProperties
import ru.nobirds.torrent.client.LocalPeerFactory
import ru.nobirds.torrent.client.announce.AnnounceService
import ru.nobirds.torrent.client.DigestProvider

public class TorrentTaskManager(val config:Config, val digestProvider: DigestProvider) {

    private val parserService: TorrentParserImpl = TorrentParserImpl(digestProvider)
    private val localPeerFactory = LocalPeerFactory(digestProvider)

    private val tasks = ArrayList<TorrentTask>()

    public fun add(torrent:InputStream) {
        add(parserService.parse(torrent))
    }

    public fun add(torrent:Torrent) {
        val directory = config.get(ClientProperties.torrentsDirectory)

        val task = TorrentTask(
                localPeerFactory.createLocalPeer(config.get(ClientProperties.clientPortsRange)),
                directory, torrent)

        tasks.add(task)
        task.start()
    }

}