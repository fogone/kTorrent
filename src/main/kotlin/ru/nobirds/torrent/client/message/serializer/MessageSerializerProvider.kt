package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.HandshakeMessage
import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.MessageType

public class MessageSerializerProvider {

    private fun findMessageTypeByValue(t:Int): MessageType = MessageType.values().find { it.value == t }!!

    public fun marshall(message: Message, buffer:ByteBuf) {
        getMarshaller(message.messageType).write(buffer, message)
    }

    private fun getMarshaller(t:MessageType):MessageMarshaller<Message> = getSerializerImpl(t) as MessageMarshaller<Message>
    private fun getUnmarshaller(t:MessageType):MessageUnmarshaller<Message> = getSerializerImpl(t)

    public fun unmarshall(buffer: ByteBuf):Message {
        val type = buffer.readByte()
        val messageType = findMessageTypeByValue(type.toInt())
        val unmarshaller = getUnmarshaller(messageType)
        return unmarshaller.read(buffer.readableBytes(), messageType, buffer)
    }

    private fun getSerializerImpl(t:MessageType): MessageSerializer<*> = when(t) {
        MessageType.choke,
        MessageType.unchoke,
        MessageType.interested,
        MessageType.notInterested -> SimpleMessageSerializer
        MessageType.bitfield -> BitFieldMessageSerializer
        MessageType.have -> HaveMessageSerializer
        MessageType.request -> RequestMessageSerializer
        MessageType.cancel -> CancelMessageSerializer
        MessageType.piece -> PieceMessageSerializer
        MessageType.port -> PortMessageSerializer
        else -> throw IllegalArgumentException("Illegal message type $t")
    }

    fun unmarshallHandshake(buffer: ByteBuf): HandshakeMessage = HandshakeMessageSerializer.read(buffer)

    fun marshallHandshake(buffer: ByteBuf, message: HandshakeMessage) { HandshakeMessageSerializer.write(buffer, message) }

}

