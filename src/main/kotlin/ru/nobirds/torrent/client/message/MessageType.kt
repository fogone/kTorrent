package ru.nobirds.torrent.client.message

public enum class MessageType(val value:Int) {

    choke:MessageType(0)
    unchoke:MessageType(1)
    interested:MessageType(2)
    notInterested:MessageType(3)
    have:MessageType(4)
    bitfield:MessageType(5)
    request:MessageType(6)
    piece:MessageType(7)
    cancel:MessageType(8)
    port:MessageType(9)

}

