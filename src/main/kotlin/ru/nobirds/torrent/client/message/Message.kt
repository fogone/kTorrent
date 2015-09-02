package ru.nobirds.torrent.client.message

import ru.nobirds.torrent.utils.Id
import java.util.*

interface Message {

    val messageType:MessageType

}

public abstract class SimpleMessage(override val messageType:MessageType) : Message

public class HandshakeMessage(val hash: Id, val peer:Id, val protocol:String= "BitTorrent protocol") : SimpleMessage(MessageType.handshake)

public class HaveMessage(val piece:Int) : SimpleMessage(MessageType.have)

public class BitFieldMessage(val pieces:BitSet) : SimpleMessage(MessageType.bitfield)

public open class AbstractRequestMessage(type:MessageType, val index:Int, val begin:Int, val length:Int) : SimpleMessage(type)

public class RequestMessage(index:Int, begin:Int, length:Int) : AbstractRequestMessage(MessageType.request, index, begin, length)

public class CancelMessage(index:Int, begin:Int, length:Int) : AbstractRequestMessage(MessageType.cancel, index, begin, length)

public class PieceMessage(val index:Int, val begin:Int, val block:ByteArray) : SimpleMessage(MessageType.piece)

public class PortMessage(val port:Int) : SimpleMessage(MessageType.port)



