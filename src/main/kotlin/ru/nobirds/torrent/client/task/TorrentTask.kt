package ru.nobirds.torrent.client.task

import io.netty.util.internal.ConcurrentSet
import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.connection.ConnectionManager
import ru.nobirds.torrent.client.connection.PeerAndMessage
import ru.nobirds.torrent.client.message.BitFieldMessage
import ru.nobirds.torrent.client.message.HandshakeMessage
import ru.nobirds.torrent.client.message.HaveMessage
import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.PieceMessage
import ru.nobirds.torrent.client.message.RequestMessage
import ru.nobirds.torrent.client.model.TorrentInfo
import ru.nobirds.torrent.client.task.file.CompositeFileDescriptor
import ru.nobirds.torrent.client.task.file.FileDescriptor
import ru.nobirds.torrent.client.task.requirement.RequirementsStrategy
import ru.nobirds.torrent.client.task.state.BlockPositionAndBytes
import ru.nobirds.torrent.client.task.state.BlockPositionAndSize
import ru.nobirds.torrent.client.task.state.ChoppedState
import ru.nobirds.torrent.client.task.state.SimpleState
import ru.nobirds.torrent.client.task.state.State
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.log
import ru.nobirds.torrent.utils.queueHandlerThread
import ru.nobirds.torrent.utils.setBits
import java.io.Closeable
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap

public class TorrentTask(val directory:Path,
                         val torrent: TorrentInfo,
                         val digestProvider: DigestProvider,
                         val connectionManager:ConnectionManager,
                         val requirementsStrategy: RequirementsStrategy) : Closeable {

    private val logger = log()

    public val hash:Id = Id.fromBytes(torrent.hash!!)

    public val localPeer:Id = Id.random()

    public val uploadStatistics:TrafficStatistics = TrafficStatistics()

    public val downloadStatistics:TrafficStatistics = TrafficStatistics()

    private val messages = ArrayBlockingQueue<TaskMessage>(1000)

    private val state: ChoppedState = ChoppedState(torrent)

    private val files:CompositeFileDescriptor = CompositeFileDescriptor(createFiles())

    private val peersState = ConcurrentHashMap<InetSocketAddress, State>()

    private val peers:MutableSet<InetSocketAddress> = ConcurrentSet<InetSocketAddress>()

    private val handlerThread = queueHandlerThread(messages) { handleMessage(it) }

    private fun addBlock(positionAndBytes: BlockPositionAndBytes) {
        logger.debug("Block {} add for task {}", positionAndBytes, hash)

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
        logger.debug("Task {} starting rehash", hash)

        val bitSet = digestProvider.checkHashes(
                torrent.pieceLength, torrent.hashes, files.compositeRandomAccessFile)

        val doneCount = bitSet.setBits(torrent.pieceCount).count()

        logger.info("Task {} rehashed, done: {}/{}", hash, doneCount, torrent.pieceCount)

        state.done(bitSet)
    }

    private fun createFiles():List<FileDescriptor> {
        val files = torrent.files

        val parent = directory.resolve(files.name)

        if (Files.exists(parent)) {
            logger.info("Task {} files exists and will be rehashed", hash)
            sendMessage(RehashTorrentFilesMessage())
        }

        return files.files.map { FileDescriptor(parent, it) }
    }

    private fun handleMessage(message: TaskMessage) {
        when(message) {
            is HandleTaskMessage -> handleIncomingMessage(message.message)
            is RehashTorrentFilesMessage -> rehashTorrentFiles()
            is AddPeersMessage -> handleAddPeers(message.peers)
            is RequestBlockMessage -> sendRequest(message.peer, message.positionAndSize)
        }
    }

    private fun sendRequest(peer: Peer, positionAndSize: BlockPositionAndSize) {
        connectionManager.send(peer, RequestMessage(positionAndSize))
    }

    private fun handleAddPeers(peers: Set<InetSocketAddress>) {
        newPeers(peers.asSequence()).forEach { connect(hash, it) }
    }

    private fun connect(hash: Id, address: InetSocketAddress) {
        val peer = Peer(hash, address)

        logger.debug("Try connect to {}", address)

        connectionManager.connect(peer) { sendHandshake(peer) }
    }

    private fun handleIncomingMessage(message: PeerAndMessage) {
        val (peer, subMessage) = message

        when (subMessage) {
            is Message -> handleTorrentMessage(peer, subMessage)
            else -> throw IllegalArgumentException("Unsupported message for $subMessage for peer $peer")
        }
    }

    private fun handleTorrentMessage(peer: Peer, message:Message) {
        logger.debug("Torrent message {} from peer {}", message.messageType, peer)

        when (message) {
            is HandshakeMessage -> handleHandshake(peer, message)
            is BitFieldMessage -> handleBifField(peer, message)
            is HaveMessage -> handleHave(peer, message)
            is PieceMessage -> handlePiece(peer, message)
            is RequestMessage -> handleRequest(peer, message)
        }
    }

    private fun handleRequest(peer: Peer, message: RequestMessage) {
        logger.debug("Request {} from peer {}", message.positionAndSize, peer)

        if (state.isDone(message.positionAndSize.position.piece)) {
            val block = state.toGlobalBlock(message.positionAndSize)

            val bytes = files.compositeRandomAccessFile.read(block)

            connectionManager.send(peer, PieceMessage(message.positionAndSize.withBytes(bytes)))
        }
    }

    private fun handleHave(peer: Peer, message: HaveMessage) {
        logger.debug("Have {} piece from peer {}", message.piece, peer)

        val peerTorrentState = getPeerTorrentState(peer.address)
        peerTorrentState.done(message.piece)

        requirementsStrategy.next(state, peerTorrentState, 10).forEach {
            logger.debug("Request {} to peer {}", it, peer)

            sendMessage(RequestBlockMessage(peer, it))
        }
    }

    private fun handleBifField(peer: Peer, message: BitFieldMessage) {
        logger.debug("Bitfield {}/{} from peer {}", message.pieces.setBits(torrent.pieceCount), torrent.pieceCount, peer)

        val peerTorrentState = getPeerTorrentState(peer.address)
        peerTorrentState.done(message.pieces)

        requirementsStrategy.next(state, peerTorrentState, 10).forEach {
            logger.debug("Request {} to peer {}", it, peer)

            sendMessage(RequestBlockMessage(peer, it))
        }
    }

    private fun handlePiece(peer:Peer, message: PieceMessage) {
        addBlock(message.positionAndBytes)
    }

    private fun newPeers(peers:Sequence<InetSocketAddress>):Sequence<InetSocketAddress> {
        val new = peers.filter { it !in this.peers }.toList()

        return if (new.isNotEmpty()) {
            logger.debug("Found {} new peers for task {}", new.size(), hash)

            this.peers.addAll(new)
            new.asSequence()
        } else emptySequence()
    }

    override fun close() {
        handlerThread.interrupt()
    }

    private fun getPeerTorrentState(peer: InetSocketAddress) = peersState.concurrentGetOrPut(peer) { SimpleState(torrent.pieceCount) }

    private fun handleHandshake(peer: Peer, message: HandshakeMessage) {
        peers.add(peer.address)

        if (!message.complete) {
            sendHandshake(peer)
        }

        sendState(peer, piecesState)
    }

    private fun sendState(peer: Peer, state: State) {
        logger.debug("Sending out state to peer {}", peer)

        connectionManager.send(peer, BitFieldMessage(state.getBits()))
    }

    private fun sendHandshake(peer:Peer) {
        logger.debug("Sending handshake to peer {}", peer)

        connectionManager.send(peer, HandshakeMessage(hash, localPeer))
    }


}