package ru.nobirds.torrent.client.message

public enum class MessageType(val value:Int) {

    handshake('B'.toInt()),
    choke(0),
    unchoke(1),
    interested(2),
    notInterested(3),
    have(4),
    bitfield(5),
    request(6),
    piece(7),
    cancel(8),
    port(9)

}

