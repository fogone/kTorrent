package ru.nobirds.torrent.client.message

enum class MessageType(val value:Int) {

    handshake(-1),
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

