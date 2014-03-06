package ru.nobirds.torrent.client.message

import java.io.DataOutputStream
import java.io.DataInputStream

public object SimpleMessageSerializer : MessageSerializer<SimpleMessage> {

    override fun read(length: Int, messageType: MessageType, stream: DataInputStream): SimpleMessage
            = SimpleMessage(messageType)

    override fun write(stream: DataOutputStream, message: SimpleMessage) {
        stream.writeInt(1)
        stream.writeByte(message.messageType.value)
    }

}
