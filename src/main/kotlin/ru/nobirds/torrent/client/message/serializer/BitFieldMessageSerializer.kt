package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.BitFieldMessage
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.client.task.state.SimpleState
import java.util.*

public object BitFieldMessageSerializer : MessageSerializer<BitFieldMessage> {

    override fun read(length: Int, messageType: MessageType, stream: ByteBuf): BitFieldMessage {
        val buffer = ByteArray(length)
        stream.readBytes(buffer)
        return BitFieldMessage(BitSet.valueOf(buffer))
    }

    override fun write(stream: ByteBuf, message: BitFieldMessage) {
        val bytes = message.pieces.toByteArray()

        stream.writeInt(bytes.size() + 1)
        stream.writeByte(message.messageType.value)
        stream.writeBytes(bytes)
    }

}