package ru.nobirds.torrent.client.model

public data class TorrentInfo(
        val hash:ByteArray,
        val pieceLength:Long,
        val hashes:List<String>,
        val files:TorrentFiles) {

    val pieceCount:Int
        get() = hashes.size

}
