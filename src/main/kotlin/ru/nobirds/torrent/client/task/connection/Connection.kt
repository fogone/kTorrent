package ru.nobirds.torrent.client.task.connection

import ru.nobirds.torrent.client.message.Message
import java.net.Socket
import ru.nobirds.torrent.client.task.TorrentTask
import ru.nobirds.torrent.client.task.state.TorrentState
import ru.nobirds.torrent.client.message.SimpleMessage
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.closeQuietly
import ru.nobirds.torrent.client.message.BitFieldMessage
import java.util.HashSet
import ru.nobirds.torrent.client.task.state.FreeBlockIndex
import ru.nobirds.torrent.client.message.RequestMessage
import ru.nobirds.torrent.client.message.CancelMessage
import ru.nobirds.torrent.client.message.PieceMessage
import ru.nobirds.torrent.client.message.HaveMessage

public class Connection(val task:TorrentTask, val socket:Socket) {


    private val requested = HashSet<FreeBlockIndex>()

    private val output = OutputConnectionStreamThread(task, socket.getOutputStream()!!)
    private val input = InputConnectionStreamThread(task, ConnectionInputStream(socket.getInputStream()!!), output)

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

