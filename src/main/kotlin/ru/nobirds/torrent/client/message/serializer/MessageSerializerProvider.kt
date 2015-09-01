package ru.nobirds.torrent.client.message.serializer

import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.MessageType

public class MessageSerializerProvider {

    public fun findMessageTypeByValue(t:Int): MessageType = MessageType.values().find { it.value == t }!!

    public fun getSerializerByType<T: Message>(t:Int):MessageSerializer<T> = getSerializer(findMessageTypeByValue(t))

    public fun getSerializer<T:Message>(t:MessageType):MessageSerializer<T> = getSerializerImpl(t) as MessageSerializer<T>

    private fun getSerializerImpl(t:MessageType): MessageSerializer<out Message> = when(t) {
        MessageType.choke,
        MessageType.unchoke,
        MessageType.interested,
        MessageType.interested -> SimpleMessageSerializer
        MessageType.bitfield -> BitFieldMessageSerializer
        MessageType.have -> HaveMessageSerializer
        MessageType.request,
        MessageType.cancel -> RequestMessageSerializer
        MessageType.piece -> PieceMessageSerializer
        MessageType.port -> PortMessageSerializer
        else -> throw IllegalArgumentException("Illegal message type ${t}")
    }
}

