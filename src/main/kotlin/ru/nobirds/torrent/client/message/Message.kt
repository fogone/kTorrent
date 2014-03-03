package ru.nobirds.torrent.client.message

import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ArrayBlockingQueue
import java.util.BitSet

trait Message {

    val messageType:MessageType

}

public open class SimpleMessage(override val messageType:MessageType) : Message

public class BitFieldMessage(val pieces:BitSet) : SimpleMessage(MessageType.bitfield)

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

    }

    override fun write(stream: OutputStream, message: BitFieldMessage) {

    }
}

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

public class MessageQueue {

    private val blokingQueue = ArrayBlockingQueue<Message>(5000)

    public fun add(message:Message) {
        blokingQueue.put(message)
    }

    public fun get():Message = blokingQueue.take()!!

}