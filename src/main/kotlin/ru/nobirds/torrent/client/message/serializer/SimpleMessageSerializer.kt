package ru.nobirds.torrent.client.message

import java.io.DataOutputStream

public object SimpleMessageSerializer : AbstractMessageSerializer<SimpleMessage>() {

    override fun deserialize(messageType: MessageType, body: ByteArray): SimpleMessage = SimpleMessage(messageType)

    override fun write(stream: DataOutputStream, message: SimpleMessage) {
        stream.writeInt(1)
        stream.writeByte(message.messageType.value)
    }

}
