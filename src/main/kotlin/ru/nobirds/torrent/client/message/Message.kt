package ru.nobirds.torrent.client.message

import java.util.BitSet

interface Message {

    val messageType:MessageType

}

public open class SimpleMessage(override val messageType:MessageType) : Message

public class HandshakeMessage(val piece:Int) : SimpleMessage(MessageType.handshake)

public class HaveMessage(val piece:Int) : SimpleMessage(MessageType.have)

public class BitFieldMessage(val pieces:BitSet) : SimpleMessage(MessageType.bitfield)

public class RequestMessage(val index:Int, val begin:Int, val length:Int) : SimpleMessage(MessageType.request)

public class CancelMessage(val index:Int, val begin:Int, val length:Int) : SimpleMessage(MessageType.cancel)

public class PieceMessage(val index:Int, val begin:Int, val block:ByteArray) : SimpleMessage(MessageType.piece)

public class PortMessage(val port:Int) : SimpleMessage(MessageType.port)



