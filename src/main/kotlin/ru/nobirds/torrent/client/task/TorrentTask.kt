package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.model.Torrent
import java.util.HashMap
import java.net.URL
import ru.nobirds.torrent.client.Peer
import java.util.HashSet
import java.nio.file.Path
import java.util.ArrayList
import ru.nobirds.torrent.client.Sha1Provider

public class TorrentTask(val peer:Peer, val directory:Path, val torrent:Torrent) {

    private var taskState:TaskState = TaskState.stopped

    val uploadStatistics = TrafficStatistics()

    val downloadStatistics = TrafficStatistics()

    val files:CompositeFileDescriptor = CompositeFileDescriptor(createFiles())

    val state:TorrentState = createTorrentState()

    val peers = HashMap<URL, Set<Peer>>()

    private val connections = HashSet<Connection>()

    private fun createTorrentState():TorrentState {
        val bitSet = Sha1Provider.checkHashes(torrent.info.hashes, files.compositeRandomAccessFile)
        return TorrentState(torrent.info.hashes.size, bitSet)
    }

    private fun createFiles():List<FileDescriptor> {
        val files = torrent.info.files

        val parent = directory.resolve(files.name)!!

        return files.files.map { FileDescriptor(parent, it) }
    }

    public fun updatePeers(url:URL, peers:List<Peer>) {
        this.peers[url] = HashSet(peers)
        if(!peers.empty)
            addConnections(peers)
    }

    private fun addConnections(peers:List<Peer>) {
        peers.forEach {
            val connection = Connection(this, it)
            connections.add(connection)
            connection.start()
        }
    }

    public fun removeConnection(connection:Connection) {
        connections.remove(connection)
    }

}