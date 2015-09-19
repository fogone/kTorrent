package ru.nobirds.torrent.client.message

import ru.nobirds.torrent.utils.Id
import java.util.*

interface Message {

    val messageType:MessageType

}

public data class SimpleMessage(override val messageType:MessageType) : Message

public data class HandshakeMessage(val hash: Id, val peer: Id, val protocol: String = "BitTorrent protocol",
                                   var complete: Boolean = false) : Message {
    override val messageType:MessageType = MessageType.handshake
}

public data class HaveMessage(val piece:Int) : Message {
    override val messageType:MessageType = MessageType.have
}

public data class BitFieldMessage(val pieces:BitSet) : Message {
    override val messageType:MessageType = MessageType.bitfield
}

public abstract data class AbstractRequestMessage(override val messageType: MessageType,
                                                  val index:Int, val begin:Int, val length:Int) : Message

public class RequestMessage(index:Int, begin:Int, length:Int) :
        AbstractRequestMessage(MessageType.request, index, begin, length)

public data class CancelMessage(index:Int, begin:Int, length:Int) :
        AbstractRequestMessage(MessageType.cancel, index, begin, length)

public data class PieceMessage(val index:Int, val begin:Int, val block:ByteArray) : Message {
    override val messageType:MessageType = MessageType.piece
}

public data class PortMessage(val port:Int) : Message {
    override val messageType:MessageType = MessageType.port
}



