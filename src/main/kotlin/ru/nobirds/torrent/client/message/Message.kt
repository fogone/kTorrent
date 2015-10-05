package ru.nobirds.torrent.client.message

import ru.nobirds.torrent.client.task.state.BlockPositionAndBytes
import ru.nobirds.torrent.client.task.state.BlockPositionAndSize
import ru.nobirds.torrent.utils.Id
import java.util.BitSet

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

public interface AbstractRequestMessage : Message {

    val positionAndSize: BlockPositionAndSize

}

public data class RequestMessage(override val positionAndSize: BlockPositionAndSize) : AbstractRequestMessage {
    override val messageType: MessageType = MessageType.request
}

public data class CancelMessage(override val positionAndSize: BlockPositionAndSize) : AbstractRequestMessage {
    override val messageType: MessageType = MessageType.cancel
}

public data class PieceMessage(val positionAndBytes: BlockPositionAndBytes) : Message {
    override val messageType:MessageType = MessageType.piece
}

public data class PortMessage(val port:Int) : Message {
    override val messageType:MessageType = MessageType.port
}



