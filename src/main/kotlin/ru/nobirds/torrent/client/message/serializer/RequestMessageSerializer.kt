package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.AbstractRequestMessage
import ru.nobirds.torrent.client.message.CancelMessage
import ru.nobirds.torrent.client.message.MessageType
import ru.nobirds.torrent.client.message.RequestMessage
import ru.nobirds.torrent.client.task.state.Blocks

public abstract class AbstractRequestMessageSerializer<M: AbstractRequestMessage> : MessageSerializer<M> {

    override fun read(length: Int, messageType: MessageType, stream: ByteBuf): M {
        val index = stream.readInt()
        val begin = stream.readInt()
        val blockLength = stream.readInt()
        return create(index, begin, blockLength)
    }

    protected abstract fun create(index: Int, begin: Int, length: Int): M

    override fun write(stream: ByteBuf, message: M) {
        stream.writeInt(13)
        stream.writeByte(message.messageType.value)
        stream.writeInt(message.positionAndSize.position.piece)
        stream.writeInt(message.positionAndSize.position.begin)
        stream.writeInt(message.positionAndSize.size)
    }

}

public object RequestMessageSerializer : AbstractRequestMessageSerializer<RequestMessage>() {
    override fun create(index: Int, begin: Int, length: Int): RequestMessage = RequestMessage(Blocks.positionAndSize(index, begin, length))
}

public object CancelMessageSerializer : AbstractRequestMessageSerializer<CancelMessage>() {
    override fun create(index: Int, begin: Int, length: Int): CancelMessage = CancelMessage(Blocks.positionAndSize(index, begin, length))
}
