package ru.nobirds.torrent.client.message

import java.io.DataOutputStream
import java.util.BitSet
import java.io.DataInputStream

public object BitFieldMessageSerializer : MessageSerializer<BitFieldMessage> {

    override fun read(length: Int, messageType: MessageType, stream: DataInputStream): BitFieldMessage {
        val buffer = ByteArray(length)
        stream.readFully(buffer)
        return BitFieldMessage(BitSet.valueOf(buffer))
    }

    override fun write(stream: DataOutputStream, message: BitFieldMessage) {
        val bytes = message.pieces.toByteArray()

        stream.writeInt(bytes.size() + 1)
        stream.writeByte(message.messageType.value)
        stream.write(bytes)
    }

}