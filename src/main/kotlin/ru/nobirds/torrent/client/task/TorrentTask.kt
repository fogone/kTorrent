package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.connection.ConnectionManager
import ru.nobirds.torrent.client.message.HandshakeMessage
import ru.nobirds.torrent.client.model.TorrentInfo
import ru.nobirds.torrent.client.task.state.FreeBlockIndex
import ru.nobirds.torrent.client.task.state.TorrentState
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.peers.provider.PeerProvider
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.infiniteLoopThread
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

public class TorrentTask(val directory:Path,
                         val torrent: TorrentInfo,
                         val localPeer: Peer,
                         val peerManager: PeerProvider,
                         val connectionManager: ConnectionManager,
                         val digestProvider: DigestProvider) {

    private val torrentHash = Id.fromBytes(torrent.hash!!)

    public val uploadStatistics:TrafficStatistics = TrafficStatistics()

    public val downloadStatistics:TrafficStatistics = TrafficStatistics()

    private val messages = ArrayBlockingQueue<TaskMessage>(300)

    private val files:CompositeFileDescriptor = CompositeFileDescriptor(createFiles())

    private val state: TorrentState = TorrentState(torrent)

    private val peers = HashSet<InetSocketAddress>()

    init {
        peerManager.require(torrentHash) {
            for (peer in this.peers.minus(it.peers).toList()) {
                this.peers.add(peer)
                addConnection(peer)
            }
        }
    }

    public fun addBlock(index: FreeBlockIndex, block:ByteArray) {
        val file = files.compositeRandomAccessFile
        val blockIndex = state.freeIndexToBlockIndex(index.piece, index.begin, index.length) ?: return
        val globalIndex = state.blockIndexToGlobalIndex(blockIndex.piece, blockIndex.block)

        file.seek(globalIndex.begin.toLong())
        file.output.write(block)

        state.done(blockIndex.piece, blockIndex.block)
    }

    public fun sendMessage(message:TaskMessage) {
        messages.put(message)
    }

    private fun rehashTorrentFiles() {
        val bitSet = digestProvider.checkHashes(
                torrent.pieceLength, torrent.hashes, files.compositeRandomAccessFile)

        state.done(bitSet)
    }

    private fun createFiles():List<FileDescriptor> {
        val files = torrent.files

        val parent = directory.resolve(files.name)

        // if(files exists) sendMessage(UpdateTorrentStateMessage())

        return files.files.map { FileDescriptor(parent, it) }
    }

    private fun handleMessage(message: TaskMessage) {
        when(message) {
            is RehashTorrentFilesMessage -> rehashTorrentFiles()
        }
    }

    private fun addConnection(address: InetSocketAddress) {
        connectionManager.send(address, HandshakeMessage(torrentHash, localPeer.id))
    }
}