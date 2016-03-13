package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.connection.ConnectionManager
import ru.nobirds.torrent.client.connection.PeerAndMessage
import ru.nobirds.torrent.client.message.BitFieldMessage
import ru.nobirds.torrent.client.message.HandshakeMessage
import ru.nobirds.torrent.client.message.HaveMessage
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.client.message.PieceMessage
import ru.nobirds.torrent.client.message.RequestMessage
import ru.nobirds.torrent.client.message.SimpleMessage
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

internal data class PeerFlags(var choked: Boolean = true,
                              var interested: Boolean = false) {

    fun choked(value:Boolean) {
        this.choked = value
    }

    fun interested(value: Boolean) {
        this.interested = value
    }

}

internal data class PeerState(val address: InetSocketAddress,
                              val state: State,
                              val myFlags: PeerFlags = PeerFlags(),
                              val peerFlags: PeerFlags = PeerFlags()) {

    val uploadAvailable:Boolean
        get() = peerFlags.interested && !myFlags.choked

    val downloadAvailable:Boolean
        get() = !peerFlags.choked && myFlags.interested

}


object Peers {

    val prefix = "-kT1000-".map(Char::toByte).toByteArray()

}

class TorrentTask(val directory:Path,
                         val torrent: TorrentInfo,
                         val digestProvider: DigestProvider,
                         val connectionManager:ConnectionManager,
                         val requirementsStrategy: RequirementsStrategy) : Closeable {

    private val logger = log()

    val hash:Id = Id.fromBytes(torrent.hash!!)

    val localPeer:Id = Id.randomWithPrefix(Peers.prefix)

    val uploadStatistics:TrafficStatistics = TrafficStatistics()

    val downloadStatistics:TrafficStatistics = TrafficStatistics()

    private val messages = ArrayBlockingQueue<TaskMessage>(1000)

    private val state: ChoppedState = ChoppedState(torrent)

    private val files:CompositeFileDescriptor = CompositeFileDescriptor(createFiles())

    private val peers:MutableMap<InetSocketAddress, PeerState> = ConcurrentHashMap()

    private val handlerThread = queueHandlerThread(messages) { handleMessage(it) }

    val piecesState:State
        get() = state

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

    fun sendMessage(message:TaskMessage) {
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
            is HandleTaskMessage -> handleTorrentMessage(message.message)
            is RehashTorrentFilesMessage -> rehashTorrentFiles()
            is AddPeersMessage -> handleAddPeers(message.peers)
            is RequestBlockMessage -> sendRequest(message.peer, message.positionAndSize)
        }
    }

    private fun sendRequest(peer: Peer, positionAndSize: BlockPositionAndSize) {
        connectionManager.write(peer, RequestMessage(positionAndSize))
    }

    private fun handleAddPeers(peers: Set<InetSocketAddress>) {
        newPeers(peers.asSequence()).forEach { connect(hash, it) }
    }

    private fun connect(hash: Id, address: InetSocketAddress) {
        logger.debug("Send handshake to {}", address)

        sendHandshake(Peer(hash, address))
    }

    private fun handleTorrentMessage(peerAndMessage: PeerAndMessage) {
        val (peer, message) = peerAndMessage

        logger.debug("Torrent message {} from peer {}", message.messageType, peer)

        when (message) {
            is HandshakeMessage -> handleHandshake(peer, message)
            is BitFieldMessage -> handleBifField(peer, message)
            is SimpleMessage -> handleSimpleMessage(peer, message)
            is HaveMessage -> handleHave(peer, message)
            is PieceMessage -> handlePiece(peer, message)
            is RequestMessage -> handleRequest(peer, message)
        }
    }

    private fun handleSimpleMessage(peer: Peer, message: SimpleMessage) {
        when (message.messageType) {
            MessageType.choke -> handleChoke(peer)
            MessageType.unchoke -> handleUnchoke(peer)
            MessageType.interested -> handleInterested(peer)
            MessageType.notInterested -> handleNotInterested(peer)
            else -> throw IllegalArgumentException("Unknown message $message")
        }
    }

    private fun handleChoke(peer: Peer) {
        logger.debug("Peer {} choke communication", peer)

        peerState(peer).peerFlags.choked(true)
    }

    private fun handleNotInterested(peer: Peer) {
        logger.debug("Peer {} not interested in communication more", peer)

        peerState(peer).peerFlags.interested(false)
    }

    private fun handleInterested(peer: Peer) {
        logger.debug("Peer {} now interested in communication", peer)

        val peerFlags = peerState(peer).peerFlags

        peerFlags.interested(true)
    }

    private fun handleUnchoke(peer: Peer) {
        logger.debug("Peer {} unchoke communication", peer)

        val peerFlags = peerState(peer).peerFlags

        peerFlags.choked(false)

        requestBlocks(peer, 10)
    }

    private fun requestBlocks(peer: Peer, count: Int) {
        val peerState = peerState(peer)

        if (peerState.downloadAvailable) {
            requirementsStrategy.next(state, peerState.state, count).forEach {
                logger.debug("Request {} to peer {}", it, peer)

                sendMessage(RequestBlockMessage(peer, it))
            }
        } else {
            logger.debug("Request to peer {} rejected, cause it choke as.", peer)
        }

    }

    private fun peerState(peer: Peer) = peerState(peer.address)
    private fun peerState(address: InetSocketAddress) = peers[address] ?:
            throw IllegalStateException("No state for peer $address")

    private fun handleRequest(peer: Peer, message: RequestMessage) {
        logger.debug("Request {} from peer {}", message.positionAndSize, peer)

        val peerState = peerState(peer)

        if (peerState.uploadAvailable) {
            if (state.isDone(message.positionAndSize.position.piece)) {
                val block = state.toGlobalBlock(message.positionAndSize)

                val bytes = files.compositeRandomAccessFile.read(block)

                connectionManager.write(peer, PieceMessage(message.positionAndSize.withBytes(bytes)))
            } else {
                logger.warn("Peer {} request piece which not existed in local state: {}", peer, message.positionAndSize)
            }
        } else {
            logger.warn("Peer {} send request in unexpected state: peer choked {}, me interested {}",
                    peer, peerState.peerFlags.choked, peerState.myFlags.interested)
        }
    }

    private fun handleHave(peer: Peer, message: HaveMessage) {
        logger.debug("Have {} piece from peer {}", message.piece, peer)

        val peerTorrentState = peerState(peer.address).state

        peerTorrentState.done(message.piece)
    }

    private fun handleBifField(peer: Peer, message: BitFieldMessage) {
        logger.debug("Bitfield {}/{} from peer {}", message.pieces.setBits(torrent.pieceCount), torrent.pieceCount, peer)

        val peerState = peerState(peer)

        val peerTorrentState = peerState.state

        peerTorrentState.done(message.pieces)

        // todo: make decision
        connectionManager.write(peer, SimpleMessage(MessageType.unchoke))

/*
        if (!state.contains(peerTorrentState)) {
        }
*/
        connectionManager.write(peer, SimpleMessage(MessageType.interested))

        peerState.myFlags.choked(false)
        peerState.myFlags.interested(true)
    }

    private fun handlePiece(peer:Peer, message: PieceMessage) {
        addBlock(message.positionAndBytes)
        requestBlocks(peer, 1)
    }

    private fun newPeers(peers:Sequence<InetSocketAddress>):Sequence<InetSocketAddress> {
        val new = peers.filter { it !in this.peers }.toList()

        return if (new.isNotEmpty()) {
            logger.debug("Found {} new peers for task {}", new.size, hash)

            new.map { PeerState(it, SimpleState(torrent.pieceCount)) }.associateByTo(this.peers) { it.address }

            new.asSequence()
        } else emptySequence()
    }

    override fun close() {
        handlerThread.interrupt()
    }

    private fun handleHandshake(peer: Peer, message: HandshakeMessage) {
        if (!message.complete) {
            sendHandshake(peer)
        }

        sendState(peer, piecesState)
    }

    private fun sendState(peer: Peer, state: State) {
        logger.debug("Sending out state to peer {}", peer)

        connectionManager.write(peer, BitFieldMessage(state.getBits()))
    }

    private fun sendHandshake(peer:Peer) {
        logger.debug("Sending handshake to peer {}", peer)

        connectionManager.write(peer, HandshakeMessage(hash, localPeer))
    }


}