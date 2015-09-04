package ru.nobirds.torrent.client.message.serializer

import io.netty.buffer.ByteBuf
import ru.nobirds.torrent.client.message.Message
import ru.nobirds.torrent.client.message.MessageType

public interface MessageMarshaller<in T:Message> {

    fun write(stream:ByteBuf, message:T)

}

public interface MessageUnmarshaller<out T:Message> {

    fun read(length:Int, messageType: MessageType, stream:ByteBuf):T

}

public interface MessageSerializer<T: Message> : MessageMarshaller<T>, MessageUnmarshaller<T>

