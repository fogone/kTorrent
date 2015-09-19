package ru.nobirds.torrent.client.task


import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.connection.ConnectionManager
import ru.nobirds.torrent.client.connection.PeerAndMessage
import ru.nobirds.torrent.client.model.Torrent
import ru.nobirds.torrent.client.task.requirement.SimpleRequirementsStrategy
import ru.nobirds.torrent.parser.TorrentParser
import ru.nobirds.torrent.peers.provider.PeerProvider
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.infiniteLoopThread
import ru.nobirds.torrent.utils.log
import java.io.InputStream
import java.nio.file.Path
import java.util.*

public class TaskManager(val directory: Path,
                         val peerManager: PeerProvider,
                         val connectionManager: ConnectionManager,
                         val parserService: TorrentParser,
                         val digestProvider: DigestProvider) {

    private val logger = log()

    private val tasks = HashMap<Id, TorrentTask>()

    init {
        infiniteLoopThread {
            handleMessage(connectionManager.read())
        }
    }

    private fun handleMessage(message: PeerAndMessage) {
        val task = task(message.peer.hash)

        logger.debug("Message {} translated to task {}", message.message.messageType, task.hash)

        task.sendMessage(HandleTaskMessage(message))
    }

    public fun add(torrent:InputStream, target:Path = directory) {
        add(parserService.parse(torrent), target)
    }

    public fun add(torrent:Torrent, target:Path = directory) {
        val id = Id.fromBytes(torrent.info.hash!!)

        if (id !in tasks.keySet()) {
            addTask(createTask(target, torrent))
        } else {
            task(id).sendMessage(RehashTorrentFilesMessage())
        }
    }

    private fun addTask(task: TorrentTask) {
        tasks.put(task.hash, task)
    }

    private fun task(hash:Id):TorrentTask = tasks.get(hash) ?: throw IllegalArgumentException()

    private fun createTask(target: Path, torrent: Torrent): TorrentTask {
        val task = TorrentTask(target, torrent.info, digestProvider, connectionManager, SimpleRequirementsStrategy())

        peerManager.require(task.hash) {
            task.sendMessage(AddPeersMessage(it.peers))
        }

        return task
    }

}