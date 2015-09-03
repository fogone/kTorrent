package ru.nobirds.torrent.client.task

import io.netty.util.internal.ConcurrentSet
import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.connection.ConnectionManager
import ru.nobirds.torrent.client.connection.PeerAndMessage
import ru.nobirds.torrent.client.message.BitFieldMessage
import ru.nobirds.torrent.client.message.HandshakeMessage
import ru.nobirds.torrent.client.message.PieceMessage
import ru.nobirds.torrent.client.message.RequestMessage
import ru.nobirds.torrent.client.model.TorrentInfo
import ru.nobirds.torrent.client.task.file.CompositeFileDescriptor
import ru.nobirds.torrent.client.task.file.FileDescriptor
import ru.nobirds.torrent.client.task.requirement.RequirementsStrategy
import ru.nobirds.torrent.client.task.state.ChoppedState
import ru.nobirds.torrent.client.task.state.FreeBlockIndex
import ru.nobirds.torrent.client.task.state.SimpleState
import ru.nobirds.torrent.client.task.state.State
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.queueHandlerThread
import java.io.Closeable
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap

public class TorrentTask(val directory:Path,
                         val torrent: TorrentInfo,
                         val digestProvider: DigestProvider,
                         val connectionManager:ConnectionManager,
                         val requirementsStrategy: RequirementsStrategy) : Closeable {

    public val hash:Id = Id.fromBytes(torrent.hash!!)

    public val localPeer:Id = Id.random()

    public val uploadStatistics:TrafficStatistics = TrafficStatistics()

    public val downloadStatistics:TrafficStatistics = TrafficStatistics()

    private val messages = ArrayBlockingQueue<TaskMessage>(1000)

    private val files:CompositeFileDescriptor = CompositeFileDescriptor(createFiles())

    private val state: ChoppedState = ChoppedState(torrent)
    private val peersState = ConcurrentHashMap<Id, State>()

    private val peers:MutableSet<InetSocketAddress> = ConcurrentSet<InetSocketAddress>()

    private val handlerThread = queueHandlerThread(messages) { handleMessage(it) }

    private fun addBlock(piece: Int, begin:Int, block:ByteArray) {
        addBlock(FreeBlockIndex(piece, begin, block.size()), block)
    }

    private fun addBlock(index: FreeBlockIndex, block:ByteArray) {
/*
        val file = files.compositeRandomAccessFile
        val blockIndex = state.freeIndexToBlockIndex(index.piece, index.begin, index.length) ?: return
        val globalIndex = state.blockIndexToGlobalIndex(blockIndex.piece, blockIndex.block)

        file.seek(globalIndex.begin.toLong())
        file.write(block)

        state.done(blockIndex.piece, blockIndex.block)

        downloadStatistics.process(block.size().toLong())
*/
    }

    public val piecesState:State
        get() = state

    public fun sendMessage(message:TaskMessage) {
        messages.put(message)
    }

    private fun rehashTorrentFiles() {
        val bitSet = digestProvider.checkHashes(
                torrent.pieceLength, torrent.hashes, files.compositeRandomAccessFile)

        state.done(bitSet.toByteArray())
    }

    private fun createFiles():List<FileDescriptor> {
        val files = torrent.files

        val parent = directory.resolve(files.name)

        // if(files exists) sendMessage(UpdateTorrentStateMessage())

        return files.files.map { FileDescriptor(parent, it) }
    }

    private fun handleMessage(message: TaskMessage) {
        when(message) {
            is HandleTaskMessage -> handleIncomingMessage(message.message)
            is RehashTorrentFilesMessage -> rehashTorrentFiles()
            is AddPeersMessage -> handleAddPeers(message.peers)
            is RequestBlockMessage -> sendRequest(message.peer.address, message.index)
        }
    }

    private fun sendRequest(address: InetSocketAddress, index: FreeBlockIndex) {
        connectionManager.send(address, RequestMessage(index.piece, index.begin, index.length))
    }

    private fun handleAddPeers(peers: Set<InetSocketAddress>) {
        newPeers(peers.asSequence()).forEach { sendHandshake(hash, it) }
    }

    private fun handleIncomingMessage(message: PeerAndMessage) {
        when (message.message) {
            is HandshakeMessage -> handleHandshake(message.peer, message.message)
            is BitFieldMessage -> handleBifField(message.peer, message.message)
            is PieceMessage -> handlePiece(message.peer.id, message.message)
        }
    }

    private fun handleBifField(peer: Peer, message: BitFieldMessage) {
        val peerTorrentState = getPeerTorrentState(peer.id)
        peerTorrentState.done(message.pieces)

        requirementsStrategy.next(state, peerTorrentState, 10).forEach {
            sendMessage(RequestBlockMessage(peer, it))
        }

    }

    private fun handlePiece(peer:Id, message: PieceMessage) {
        addBlock(message.index, message.begin, message.block)
    }

    private fun newPeers(peers:Sequence<InetSocketAddress>):Sequence<InetSocketAddress>
            = peers.filter { it !in this.peers }

    override fun close() {
        handlerThread.interrupt()
    }

    private fun getPeerTorrentState(peer: Id) = peersState.concurrentGetOrPut(peer) { SimpleState(state.count) }

    private fun handleHandshake(peer: Peer, message: HandshakeMessage) {
        peers.add(peer.address)
        sendState(peer, piecesState)
    }

    private fun sendState(peer: Peer, state: State) {
        connectionManager.send(peer.address, BitFieldMessage(state))
    }

    private fun sendHandshake(hash:Id, address: InetSocketAddress) {
        connectionManager.send(address, HandshakeMessage(hash, localPeer))
    }


}