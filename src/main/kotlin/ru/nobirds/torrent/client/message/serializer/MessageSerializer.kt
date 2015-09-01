package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.MessageType

public interface MessageSerializer<T: Message> {

    fun read(length:Int, messageType: MessageType, stream:ByteBuf):T

    fun write(stream:ByteBuf, message:T)

}

