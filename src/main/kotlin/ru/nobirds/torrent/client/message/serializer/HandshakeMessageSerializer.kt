package ru.nobirds.torrent.client.message

import java.io.DataOutputStream
import java.io.DataInputStream

public object HandshakeMessageSerializer : MessageSerializer<SimpleMessage> {

    override fun read(length: Int, messageType: MessageType, stream: DataInputStream): SimpleMessage {
        stream.skip(length.toLong())
        return SimpleMessage(messageType)
    }

    override fun write(stream: DataOutputStream, message: SimpleMessage) {
        stream.writeInt(19)
        stream.write("BitTorrent protocol".toByteArray("UTF-8"))
    }

}
