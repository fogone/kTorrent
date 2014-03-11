package ru.nobirds.torrent.client.task.connection

import ru.nobirds.torrent.client.message.MessageSerializerFactory
import ru.nobirds.torrent.client.message.Message
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.io.DataOutputStream
import java.io.OutputStream
import java.util.HashSet
import ru.nobirds.torrent.client.task.state.FreeBlockIndex
import ru.nobirds.torrent.client.task.TorrentTask
import ru.nobirds.torrent.client.message.PieceMessage
import ru.nobirds.torrent.client.message.SimpleMessage
import ru.nobirds.torrent.client.message.MessageType

class OutputConnectionStreamThread(val task:TorrentTask, val stream:OutputStream) : Thread("Output connection stream") {

    private val output = DataOutputStream(stream)

    private val queue:BlockingQueue<Any> = ArrayBlockingQueue(3000)

    public fun sendBlock(index:FreeBlockIndex) {
        queue.put(index)
    }

    public fun cancelBlock(index:FreeBlockIndex) {
        queue.remove(index)
    }

    public fun sendMessage(message:Message) {
        queue.put(message)
    }

    override fun run() {
        queue.put(SimpleMessage(MessageType.handshake))

        while(!isInterrupted()) {
            val message = queue.take()!!
            when(message) {
                is Message -> writeMessage(message)
                is FreeBlockIndex -> writeBlock(message)
            }
        }
    }

    private fun writeBlock(index:FreeBlockIndex) {
        if(task.state.isDone(index.piece)) {
            val globalIndex = task.state.freeIndexToGlobalIndex(index.piece, index.begin, index.length)
            val byteArray = task.files.compositeRandomAccessFile.read(globalIndex)
            writeMessage(PieceMessage(index.piece, index.begin, byteArray))
        }
    }

    private fun writeMessage(message:Message) {
        MessageSerializerFactory.getSerializer<Message>(message.messageType).write(output, message)
    }

}

