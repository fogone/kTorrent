package ru.nobirds.torrent.client.message

import java.util.BitSet

trait Message {

    val messageType:MessageType

}

public open class SimpleMessage(override val messageType:MessageType) : Message

public class HaveMessage(val piece:Int) : SimpleMessage(MessageType.have)

public class BitFieldMessage(val pieces:BitSet) : SimpleMessage(MessageType.bitfield)

public class RequestMessage(val index:Int, val begin:Int, val length:Int) : SimpleMessage(MessageType.request)


