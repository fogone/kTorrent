package ru.nobirds.torrent.client.message

import java.io.DataOutputStream
import java.util.BitSet
import java.io.DataInputStream

public object PortMessageSerializer : MessageSerializer<PortMessage> {

    override fun read(length: Int, messageType: MessageType, stream: DataInputStream): PortMessage
            = PortMessage(stream.readShort().toInt() and 0xffff)

    override fun write(stream: DataOutputStream, message: PortMessage) {
        stream.writeInt(5)
        stream.writeByte(message.messageType.value)
        stream.writeShort(message.port)
    }

}