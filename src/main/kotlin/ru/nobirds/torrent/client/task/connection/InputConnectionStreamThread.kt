package ru.nobirds.torrent.client.task.connection

import ru.nobirds.torrent.client.message.MessageSerializerFactory
import ru.nobirds.torrent.client.message.Message
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.io.DataInputStream
import java.io.InputStream
import ru.nobirds.torrent.client.message.SimpleMessage
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.client.message.BitFieldMessage
import ru.nobirds.torrent.client.message.HaveMessage
import ru.nobirds.torrent.client.message.RequestMessage
import ru.nobirds.torrent.client.message.CancelMessage
import ru.nobirds.torrent.client.task.state.FreeBlockIndex
import ru.nobirds.torrent.client.message.PieceMessage
import ru.nobirds.torrent.client.task.TorrentTask
import ru.nobirds.torrent.client.task.state.TorrentState

class InputConnectionStreamThread(val task:TorrentTask, val peerState:TorrentState,  val stream:InputStream, val output:OutputConnectionStreamThread) : Thread("Connection handler thread") {

    private val input = DataInputStream(stream)

    override fun run() {
        val handshake = receive()

        if(handshake.messageType != MessageType.handshake) {
            // todo
            throw IllegalArgumentException()
        }

        while(!isInterrupted()) {
            handle(receive())
        }
    }

    private fun handle(message:Message) {
        when(message) {
            is BitFieldMessage -> peerState.done(message.pieces)
            is HaveMessage -> peerState.done(message.piece)
            is RequestMessage -> output.sendBlock(FreeBlockIndex(message.index, message.begin, message.length))
            is CancelMessage -> output.cancelBlock(FreeBlockIndex(message.index, message.begin, message.length))
            is PieceMessage -> task.addBlock(FreeBlockIndex(message.index, message.begin, message.block.size), message.block)
        }
    }

    public fun receive():Message {
        val length = input.readInt()
        val messageType = MessageSerializerFactory.findMessageTypeByValue(stream.read())

        return MessageSerializerFactory
                .getSerializer<Message>(messageType)
                .read(length - 1, messageType, input)
    }

}
