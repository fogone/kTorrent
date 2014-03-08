package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.model.Torrent
import java.util.HashMap
import java.net.URL
import ru.nobirds.torrent.client.Peer
import java.util.HashSet
import java.nio.file.Path
import ru.nobirds.torrent.client.Sha1Provider
import java.net.Socket
import ru.nobirds.torrent.client.task.connection.ConnectionListener
import java.util.concurrent.ArrayBlockingQueue
import ru.nobirds.torrent.client.message.BitFieldMessage
import ru.nobirds.torrent.client.task.tracker.Tracker
import java.util.ArrayList
import ru.nobirds.torrent.client.task.tracker.HttpUrlTracker
import java.util.Timer
import ru.nobirds.torrent.client.announce.AnnounceService
import ru.nobirds.torrent.client.task.connection.Connection
import ru.nobirds.torrent.client.task.state.FreeBlockIndex
import ru.nobirds.torrent.client.task.state.TaskState
import ru.nobirds.torrent.client.task.state.TorrentState

public class TorrentTask(val peer:Peer, val directory:Path, val torrent:Torrent) : ConnectionListener, Thread("Torrent task") {

    private val announceService = AnnounceService()

    private val timer = Timer()

    private var taskState: TaskState = TaskState.stopped

    val uploadStatistics = TrafficStatistics()

    val downloadStatistics = TrafficStatistics()

    private val trackers = ArrayList<Tracker>()

    private val messages = createInitialQueue()

    private val files:CompositeFileDescriptor = CompositeFileDescriptor(createFiles())

    val state: TorrentState = TorrentState(torrent.info)

    private val peers = HashSet<Peer>()

    private val connections = HashSet<Connection>()

    public fun addBlock(index: FreeBlockIndex, block:ByteArray) {
//        files.write(index, block)
//        state.done(index)
    }

    private fun createInitialQueue():ArrayBlockingQueue<TaskMessage> {
        val queue = ArrayBlockingQueue<TaskMessage>(300)
        queue.add(InitializeTrackersMessage())
        return queue
    }

    public fun sendMessage(message:TaskMessage) {
        messages.put(message)
    }

    private fun rehashTorrentFiles() {
        val bitSet = Sha1Provider.checkHashes(
                torrent.info.pieceLength, torrent.info.hashes, files.compositeRandomAccessFile)

        state.done(bitSet)
    }

    private fun createFiles():List<FileDescriptor> {
        val files = torrent.info.files

        val parent = directory.resolve(files.name)!!

        // if(files exists) sendMessage(UpdateTorrentStateMessage())

        return files.files.map { FileDescriptor(parent, it) }
    }

    private fun createConnections(peers:List<Peer>) {
        peers.filter { it !in this.peers }.filter {
            try {
                onConnection(Socket(peer.address.getAddress(), peer.address.getPort()))
                true
            } catch(e:Exception) {
                false
            }
        }.to(this.peers)
    }

    public fun removeConnection(connection:Connection) {
        connections.remove(connection)
    }

    override fun onConnection(socket: Socket) {
        sendMessage(AddConnectionMessage(socket))
    }

    private fun createAndStartConnectionForSocket(socket:Socket) {
        val connection = Connection(this, socket)
        connections.add(connection)
        connection.start()
    }

    private fun initializeTrackers() {
        for (url in torrent.announce.allUrls) {
            val tracker = HttpUrlTracker(timer, announceService, this, URL(url))
            trackers.add(tracker)
            tracker.registerUpdateListener { sendMessage(UpdatePeersMessage(it)) }
        }
    }

    override fun run() {
        while(isInterrupted().not()) {
            val message = messages.take()
            when(message) {
                is AddConnectionMessage -> createAndStartConnectionForSocket(message.socket)
                is RemoveConnectionMessage -> connections.remove(message.connection)
                is RehashTorrentFilesMessage -> rehashTorrentFiles()
                is UpdatePeersMessage -> createConnections(message.peers)
                is InitializeTrackersMessage -> initializeTrackers()
            }
        }
    }
}