package ru.nobirds.torrent.client.message

import ru.nobirds.torrent.client.task.state.State
import ru.nobirds.torrent.utils.Id
import java.util.*

interface Message {

    val messageType:MessageType

}

public data class SimpleMessage(override val messageType:MessageType) : Message

public data class HandshakeMessage(val hash: Id, val peer:Id, val protocol:String= "BitTorrent protocol", override val messageType:MessageType = MessageType.handshake) : Message

public data class HaveMessage(val piece:Int, override val messageType:MessageType = MessageType.have) : Message

public data class BitFieldMessage(val pieces:BitSet, override val messageType:MessageType = MessageType.bitfield) : Message

public abstract data class AbstractRequestMessage(override val messageType: MessageType, val index:Int, val begin:Int, val length:Int) : Message

public class RequestMessage(index:Int, begin:Int, length:Int) : AbstractRequestMessage(MessageType.request, index, begin, length)

public data class CancelMessage(index:Int, begin:Int, length:Int) : AbstractRequestMessage(MessageType.cancel, index, begin, length)

public data class PieceMessage(val index:Int, val begin:Int, val block:ByteArray, override val messageType:MessageType = MessageType.piece) : Message

public data class PortMessage(val port:Int, override val messageType:MessageType = MessageType.port) : Message



