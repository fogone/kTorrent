package ru.nobirds.torrent.client.task

import java.util.ArrayList
import ru.nobirds.torrent.client.model.Torrent


import ru.nobirds.torrent.parser.TorrentParserImpl
import java.io.InputStream
import ru.nobirds.torrent.config.Config
import ru.nobirds.torrent.client.ClientProperties
import ru.nobirds.torrent.peers.LocalPeerFactory
import ru.nobirds.torrent.announce.HttpAnnounceProvider
import ru.nobirds.torrent.client.DigestProvider
import java.nio.file.Path
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.peers.provider.PeerProvider

public class TaskManager(val directory: Path,
                                val localPeer:Peer,
                                val peerManager: PeerProvider,
                                val digestProvider: DigestProvider) {

    private val parserService: TorrentParserImpl = TorrentParserImpl(digestProvider)

    private val tasks = ArrayList<TorrentTask>()

    public fun add(torrent:InputStream) {
        add(parserService.parse(torrent))
    }

    public fun add(torrent:Torrent, target:Path = directory) {
        val task = TorrentTask(target, torrent.info, digestProvider)

        tasks.add(task)
    }

}