package ru.nobirds.torrent.client

import io.netty.buffer.Unpooled
import org.junit.Assert
import org.junit.Test
import ru.nobirds.torrent.client.message.*
import ru.nobirds.torrent.client.message.serializer.MessageSerializerProvider
import ru.nobirds.torrent.client.task.state.SimpleState
import ru.nobirds.torrent.utils.Id

public class SerializerTest {

    private val buffer = Unpooled.buffer(10 * 1024)
    private val provider = MessageSerializerProvider()

    @Test
    public fun simpleTest() {
        assertMessages(SimpleMessage(MessageType.choke))
    }

    @Test
    public fun handshakeTest() {
        assertMessages(HandshakeMessage(Id.random(), Id.random()))
    }

    @Test
    public fun bitfieldTest() {
        val simpleState = SimpleState(10)
        simpleState.done(4)
        simpleState.done(7)
        assertMessages(BitFieldMessage(simpleState.getBits()))
    }

    @Test
    public fun haveTest() {
        assertMessages(HaveMessage(5))
    }

    @Test
    public fun requestTest() {
        assertMessages(RequestMessage(10, 12345, 1024))
    }

    @Test
    public fun cancelTest() {
        assertMessages(CancelMessage(10, 12345, 1024))
    }

    @Test
    public fun pieceTest() {
        assertMessages(PieceMessage(10, 12345, ByteArray(10)))
    }

    @Test
    public fun portTest() {
        assertMessages(PortMessage(1010))
    }

    private fun assertMessages(message: Message) {
        buffer.clear()

        provider.marshall(message, buffer)

        val unmarshalledMessage = provider.unmarshall(buffer)

        Assert.assertEquals(message, unmarshalledMessage)
    }

}