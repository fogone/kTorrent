package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.model.Torrent
import java.net.URL
import ru.nobirds.torrent.client.Peer
import java.nio.file.Path
import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.task.state.FreeBlockIndex
import ru.nobirds.torrent.client.task.state.TorrentState
import akka.actor.UntypedActor
import ru.nobirds.torrent.utils.toHexString
import ru.nobirds.torrent.utils.actorOf
import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.PieceMessage
import ru.nobirds.torrent.client.message.RequestMessage
import ru.nobirds.torrent.client.message.HandshakeMessage
import ru.nobirds.torrent.client.message.BitFieldMessage
import ru.nobirds.torrent.client.message.HaveMessage
import ru.nobirds.torrent.client.announce.UpdateAnnounceMessage
import ru.nobirds.torrent.client.LocalPeerFactory

public class TorrentTask(val localPeer:Peer, val directory:Path, val torrent:Torrent, val digest: DigestProvider) : UntypedActor() {

    private val uploadStatistics = TrafficStatistics()

    private val downloadStatistics = TrafficStatistics()

    private val files:CompositeFileDescriptor = CompositeFileDescriptor(createFiles())

    private val state: TorrentState = TorrentState(torrent.info)

    private fun createFiles():List<FileDescriptor> {
        val files = torrent.info.files

        val parent = directory.resolve(files.name)!!

        // if(files exists) sendMessage(UpdateTorrentStateMessage())

        return files.files.map { FileDescriptor(parent, it) }
    }

    private fun addBlock(index: FreeBlockIndex, block:ByteArray) {
        val file = files.compositeRandomAccessFile
        val blockIndex = state.freeIndexToBlockIndex(index.piece, index.begin, index.length)

        if(blockIndex == null)
            return

        val globalIndex = state.blockIndexToGlobalIndex(blockIndex.piece, blockIndex.block)

        file.seek(globalIndex.begin.toLong())
        file.output.write(block)

        state.done(blockIndex.piece, blockIndex.block)

        notifyAddBlock(index.piece)
    }

    private fun notifyAddBlock(index:Int) {
        context()!!
                .actorSelection("peer/*")
                .tell(WriteMessageMessage(HaveMessage(index)), self())
    }

    private fun rehashTorrentFiles() {
        state.done(digest.checkHashes(torrent.info.pieceLength, torrent.info.hashes, files.compositeRandomAccessFile))
    }

    private fun createConnections(peers:List<Peer>) {
        for (peer in peers)
            startConnection(peer)
    }

    private fun startConnection(peer: Peer) {
        context()!!
                .actorOf("peer/" + peer.id.toHexString()) { PeerConnection(peer, torrent.info, self()!!) }
    }

    private fun initializeTrackers() {
        for (url in torrent.announce.allUrls) {
            val message = UpdateAnnounceMessage(
                    URL(url), localPeer,
                    torrent.info.hash!!, torrent.info.files.totalLength,
                    uploadStatistics.totalInBytes,
                    downloadStatistics.totalInBytes
            )

            getContext()!!
                    .actorSelection("/user/trakers")
                    .tell(message, getSelf())
        }
    }

    private fun sendBlock(index:FreeBlockIndex) {
        if(state.isDone(index.piece)) {
            val globalIndex = state.freeIndexToGlobalIndex(index.piece, index.begin, index.length)
            val byteArray = files.compositeRandomAccessFile.read(globalIndex)
            sendMessage(PieceMessage(index.piece, index.begin, byteArray))
        }
    }

    private fun sendMessage(message:Message) {
        sender()!!
                .tell(WriteMessageMessage(message), self())
    }

    private fun handleMessage(message:Message) {
        when(message) {
            is HandshakeMessage -> sendMessage(BitFieldMessage(state.toBitSet()))
            is RequestMessage -> sendBlock(FreeBlockIndex(message.index, message.begin, message.length))
            //is CancelMessage -> cancelBlock(FreeBlockIndex(message.index, message.begin, message.length))
            is PieceMessage -> addBlock(FreeBlockIndex(message.index, message.begin, message.block.size), message.block)
        }
    }

    override fun onReceive(message: Any?) {
        when(message) {
            is ReceiveMessageMessage -> handleMessage(message.message)
            is RehashTorrentFilesMessage -> rehashTorrentFiles()
            is UpdatePeersMessage -> createConnections(message.peers)
            is InitializeTrackersMessage -> initializeTrackers()
        }
    }
}