package ru.nobirds.torrent.client.model

public data class TorrentInfo(
        val hash:String,
        val pieceLength:Long,
        val hashes:List<String>,
        val files:TorrentFiles)
