package ru.nobirds.torrent.client.message

import java.io.DataOutputStream
import java.util.BitSet

public object BitFieldMessageSerializer : AbstractMessageSerializer<BitFieldMessage>() {

    override fun deserialize(messageType: MessageType, body: ByteArray): BitFieldMessage = BitFieldMessage(BitSet.valueOf(body))

    override fun write(stream: DataOutputStream, message: BitFieldMessage) {
        val bytes = message.pieces.toByteArray()

        stream.writeInt(bytes.size + 1)
        stream.writeByte(message.messageType.value)
        stream.write(bytes)
    }

}