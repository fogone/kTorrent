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

public class Connection(val task:TorrentTask, val socket:Socket) : Thread("Connection handler thread") {

    private val torrentState: TorrentState = TorrentState(task.torrent.info)

    private val requested = HashSet<FreeBlockIndex>()

    private val input = InputConnectionStream(ConnectionInputStream(socket.getInputStream()!!))
    private val output = OutputConnectionStreamThread(socket.getOutputStream()!!)

    fun sendMessage(message:Message) {
        output.send(message)
    }

    override fun run() {
        output.start()

        output.send(SimpleMessage(MessageType.handshake))
        val handshake = input.receive()

        if(handshake.messageType != MessageType.handshake) {
            close()
            return
        }

        while(!isInterrupted()) {
            handle(input.receive())
        }
    }

    private fun handle(message:Message) {
        when(message) {
            is BitFieldMessage -> torrentState.done(message.pieces)
            is RequestMessage -> requested.add(FreeBlockIndex(message.index, message.begin, message.length))
            is CancelMessage -> requested.remove(FreeBlockIndex(message.index, message.begin, message.length))
            is PieceMessage -> task.addBlock(FreeBlockIndex(message.index, message.begin, message.block.size), message.block)
        }
    }

    public fun close() {
        output.interrupt()
        output.join()
        socket.closeQuietly()
    }
}

