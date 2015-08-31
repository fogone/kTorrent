package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.peers.Peer
import java.util.HashSet
import java.nio.file.Path
import java.util.concurrent.ArrayBlockingQueue
import java.util.Timer
import ru.nobirds.torrent.client.task.state.FreeBlockIndex
import ru.nobirds.torrent.client.task.state.TorrentState
import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.model.TorrentInfo
import ru.nobirds.torrent.utils.Id
import java.net.InetSocketAddress
import ru.nobirds.torrent.client.connection.ConnectionManager
import ru.nobirds.torrent.peers.provider.PeerProvider

public class TorrentTask(val directory:Path,
                         val torrent: TorrentInfo,
                         val peerManager: PeerProvider,
                         val connectionManager: ConnectionManager,
                         val digestProvider: DigestProvider) {

    private val torrentHash = Id.fromBytes(torrent.hash!!)

    private val timer = Timer()

    val uploadStatistics = TrafficStatistics()

    val downloadStatistics = TrafficStatistics()

    private val messages = createInitialQueue()

    val files:CompositeFileDescriptor = CompositeFileDescriptor(createFiles())

    val state: TorrentState = TorrentState(torrent)

    private val peers = HashSet<Peer>()

    init {
        peerManager.require(Id.fromBytes(torrent.hash!!)) {
            for (peer in it.peers) {
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

    private fun createInitialQueue():ArrayBlockingQueue<TaskMessage> {
        return ArrayBlockingQueue(300)
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

    fun addConnection(address: InetSocketAddress) {
        // connectionManager.add(torrentHash, address)
    }
}