package ru.nobirds.torrent.client.message

import java.util.BitSet

trait Message {

    val messageType:MessageType

}

public open class SimpleMessage(override val messageType:MessageType) : Message

public class BitFieldMessage(val pieces:BitSet) : SimpleMessage(MessageType.bitfield)


