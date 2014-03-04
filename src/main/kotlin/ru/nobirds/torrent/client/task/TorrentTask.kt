package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.model.Torrent
import java.util.HashMap
import java.net.URL
import ru.nobirds.torrent.client.Peer
import java.util.HashSet
import java.nio.file.Path
import java.util.ArrayList
import ru.nobirds.torrent.client.Sha1Provider
import java.util.BitSet
import java.net.Socket
import ru.nobirds.torrent.client.task.connection.ConnectionListener
import java.util.concurrent.ArrayBlockingQueue
import ru.nobirds.torrent.client.message.BitFieldMessage

public trait TaskMessage

public class AddConnectionMessage(val socket:Socket) : TaskMessage
public class RemoveConnectionMessage(val connection:Connection) : TaskMessage
public class UpdatePeersMessage(val url:URL, val peers:List<Peer>) : TaskMessage
public class UpdateTorrentStateMessage() : TaskMessage

public class TorrentTask(val peer:Peer, val directory:Path, val torrent:Torrent) : ConnectionListener, Thread("Torrent task") {

    private var taskState:TaskState = TaskState.stopped

    val uploadStatistics = TrafficStatistics()

    val downloadStatistics = TrafficStatistics()

    val files:CompositeFileDescriptor = CompositeFileDescriptor(createFiles())

    val state:TorrentState = TorrentState(torrent.info.hashes.size)

    val peers = HashMap<URL, Set<Peer>>()

    private val messages = ArrayBlockingQueue<TaskMessage>(300)

    private val connections = HashSet<Connection>()

    public fun sendMessage(message:TaskMessage) {
        messages.put(message)
    }

    private fun updateTorrentState() {
        val bitSet = Sha1Provider.checkHashes(
                torrent.info.hashes, files.compositeRandomAccessFile)

        state.state.clear()
        state.state.or(bitSet)
    }

    private fun createFiles():List<FileDescriptor> {
        val files = torrent.info.files

        val parent = directory.resolve(files.name)!!

        // if(files exists) sendMessage(UpdateTorrentStateMessage())

        return files.files.map { FileDescriptor(parent, it) }
    }

    private fun updatePeers(url:URL, peers:List<Peer>) {
        this.peers[url] = HashSet(peers)
        if(!peers.empty) {
            for (peer in peers) {
                onConnection(Socket(peer.address.getAddress(), peer.address.getPort()))
            }
        }
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
        connection.sendMessage(BitFieldMessage(state.state))
        connection.start()
    }

    override fun run() {
        while(isInterrupted().not()) {
            val message = messages.take()
            when(message) {
                is AddConnectionMessage -> createAndStartConnectionForSocket(message.socket)
                is RemoveConnectionMessage -> connections.remove(message.connection)
                is UpdateTorrentStateMessage -> updateTorrentState()
                is UpdatePeersMessage -> updatePeers(message.url, message.peers)
            }
        }
    }
}