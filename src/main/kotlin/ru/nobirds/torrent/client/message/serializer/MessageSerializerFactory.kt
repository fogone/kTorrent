package ru.nobirds.torrent.client.message

import java.io.OutputStream
import java.io.InputStream
import java.util.BitSet
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer

public object MessageSerializerFactory {

    public fun findMessageTypeByValue(t:Int):MessageType = MessageType.values().find { it.value == t }!!

    public fun getSerializerByType<T:Message>(t:Int):MessageSerializer<T> = getSerializer(findMessageTypeByValue(t))

    public fun getSerializer<T:Message>(t:MessageType):MessageSerializer<T> = when(t) {
        MessageType.choke,
        MessageType.interested,
        MessageType.interested -> SimpleMessageSerializer as MessageSerializer<T>
        MessageType.bitfield -> BitFieldMessageSerializer as MessageSerializer<T>

        else -> throw IllegalArgumentException("Illegal message type ${t}")
    }

}

