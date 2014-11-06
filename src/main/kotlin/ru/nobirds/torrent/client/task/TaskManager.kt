package ru.nobirds.torrent.client.task

import java.util.ArrayList
import ru.nobirds.torrent.client.model.Torrent


import ru.nobirds.torrent.parser.TorrentParserImpl
import java.io.InputStream
import ru.nobirds.torrent.client.DigestProvider
import java.nio.file.Path
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.peers.provider.PeerProvider
import ru.nobirds.torrent.client.connection.ConnectionManager

public class TaskManager(val directory: Path,
                                val localPeer:Peer,
                                val peerManager: PeerProvider,
                                val connectionManager: ConnectionManager,
                                val digestProvider: DigestProvider) {

    private val parserService: TorrentParserImpl = TorrentParserImpl(digestProvider)

    private val tasks = ArrayList<TorrentTask>()

    public fun add(torrent:InputStream, target:Path = directory) {
        add(parserService.parse(torrent), target)
    }

    public fun add(torrent:Torrent, target:Path = directory) {
        tasks.add(TorrentTask(target, torrent.info, peerManager, connectionManager, digestProvider))
    }

}