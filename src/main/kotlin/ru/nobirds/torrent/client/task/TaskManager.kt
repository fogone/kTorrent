package ru.nobirds.torrent.client.task


import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.connection.ConnectionManager
import ru.nobirds.torrent.client.model.Torrent
import ru.nobirds.torrent.parser.TorrentParser
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.peers.provider.PeerProvider
import java.io.InputStream
import java.nio.file.Path
import java.util.*

public class TaskManager(val directory: Path,
                         val localPeer:Peer,
                         val peerManager: PeerProvider,
                         val connectionManager: ConnectionManager,
                         val parserService: TorrentParser,
                         val digestProvider: DigestProvider) {

    private val tasks = ArrayList<TorrentTask>()

    public fun add(torrent:InputStream, target:Path = directory) {
        add(parserService.parse(torrent), target)
    }

    public fun add(torrent:Torrent, target:Path = directory) {
        tasks.add(TorrentTask(target, torrent.info, peerManager, connectionManager, digestProvider))
    }

}