package ru.nobirds.torrent.client.task

import java.net.Socket
import ru.nobirds.torrent.utils.closeQuietly
import akka.actor.UntypedActor
import java.io.DataOutputStream
import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.MessageSerializerFactory
import akka.actor.ActorRef
import java.io.InputStream
import java.io.DataInputStream
import java.io.FilterInputStream
import ru.nobirds.torrent.client.Peer
import ru.nobirds.torrent.client.message.BitFieldMessage
import ru.nobirds.torrent.client.message.HaveMessage
import ru.nobirds.torrent.client.task.state.TorrentState
import ru.nobirds.torrent.client.model.TorrentInfo

public data class ConnectionClosedMessage
public data class WriteMessageMessage(val message: Message)
public data class ReceiveMessageMessage(val message: Message)

class ConnectionClosedException() : RuntimeException("Connection closed")

class ConnectionInputStream(val stream:InputStream) : FilterInputStream(stream) {

    public override fun read():Int {
        val value = stream.read()

        if(value == -1)
            throw ConnectionClosedException()

        return value
    }

}

class InputConnectionStreamThread(val stream:InputStream, val receiver:ActorRef) : Thread("Connection handler thread") {

    private val input = DataInputStream(stream)

    override fun run() {
        try {
            while(!isInterrupted()) {
                receiver.tell(ReceiveMessageMessage(receive()), ActorRef.noSender())
            }
        } catch(e: Exception) {
            receiver.tell(ConnectionClosedMessage(), ActorRef.noSender())
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

public class PeerConnection(val peer:Peer, val torrentInfo: TorrentInfo, val receiver:ActorRef) : UntypedActor() {

    private val socket = Socket(peer.address.getAddress(), peer.address.getPort())

    private val state = TorrentState(torrentInfo)

    private val input = InputConnectionStreamThread(ConnectionInputStream(socket.getInputStream()!!), self()!!)

    private val output = DataOutputStream(socket.getOutputStream()!!)

    private fun writeMessage(message:Message) {
        MessageSerializerFactory.getSerializer<Message>(message.messageType).write(output, message)
    }

    override fun onReceive(message: Any?) {
        when(message) {
            is WriteMessageMessage -> writeMessage(message.message)
            is ReceiveMessageMessage -> {
                when(message.message) {
                    is BitFieldMessage -> state.done(message.message.pieces)
                    is HaveMessage -> state.done(message.message.piece)
                    else -> receiver.forward(message, context())
                }
            }
            is ConnectionClosedMessage -> {
                socket.closeQuietly()
                context()!!.stop(self())
            }
        }
    }
}

