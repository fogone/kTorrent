package ru.nobirds.torrent.client.message

import java.io.OutputStream
import java.io.InputStream
import java.util.BitSet

public object MessageFactory {

    public fun findMessageTypeByValue(t:Int):MessageType = MessageType.values().find { it.value == t }!!

    public fun getSerializerByType(t:Int):MessageSerializer<out Message> = getSerializer(findMessageTypeByValue(t))

    public fun getSerializer(t:MessageType):MessageSerializer<out Message> = when(t) {
        MessageType.choke,
        MessageType.interested,
        MessageType.interested -> SimpleMessageSerializer
        MessageType.bitfield -> BitFieldMessageSerializer

        else -> throw IllegalArgumentException("Illegal message type ${t}")
    }

}


trait MessageSerializer<T:Message> {

    fun read(messageType:MessageType, stream:InputStream):T

    fun write(stream:OutputStream, message:T)

}

public object SimpleMessageSerializer : MessageSerializer<SimpleMessage> {

    override fun read(messageType: MessageType, stream: InputStream): SimpleMessage = SimpleMessage(messageType)

    override fun write(stream: OutputStream, message: SimpleMessage) {
        stream.write(message.messageType.value)
    }

}

public object BitFieldMessageSerializer : MessageSerializer<BitFieldMessage> {

    override fun read(messageType: MessageType, stream: InputStream): BitFieldMessage {
        return BitFieldMessage(BitSet()) // todo
    }

    override fun write(stream: OutputStream, message: BitFieldMessage) {
        // todo
    }
}