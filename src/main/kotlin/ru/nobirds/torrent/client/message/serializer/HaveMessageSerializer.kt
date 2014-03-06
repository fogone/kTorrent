package ru.nobirds.torrent.client.message

import java.io.DataOutputStream
import java.util.BitSet
import java.io.DataInputStream

public object HaveMessageSerializer : MessageSerializer<HaveMessage> {

    override fun read(length: Int, messageType: MessageType, stream: DataInputStream): HaveMessage = HaveMessage(stream.readInt())

    override fun write(stream: DataOutputStream, message: HaveMessage) {
        stream.writeInt(5)
        stream.writeByte(message.messageType.value)
        stream.writeInt(message.piece)
    }

}