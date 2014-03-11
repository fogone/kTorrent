package ru.nobirds.torrent.client.task.connection

import java.net.Socket
import ru.nobirds.torrent.client.task.TorrentTask
import ru.nobirds.torrent.client.task.state.TorrentState
import ru.nobirds.torrent.closeQuietly
import ru.nobirds.torrent.client.task.requirement.SimpleTorrentRequirements

public class Connection(val task:TorrentTask, val socket:Socket) {

    val peerState: TorrentState = TorrentState(task.torrent.info)
    val requirements = SimpleTorrentRequirements(task.state, peerState)

    private val output = OutputConnectionStreamThread(task, peerState, socket.getOutputStream()!!)
    private val input = InputConnectionStreamThread(task, peerState, ConnectionInputStream(socket.getInputStream()!!), output)

    public fun start() {
        output.start()
        input.start()
    }

    public fun close() {
        output.interrupt()
        input.interrupt()
        socket.closeQuietly()
    }
}

