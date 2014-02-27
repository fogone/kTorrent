package ru.nobirds.torrent

import java.util.Date
import java.net.URL
import java.util.Collections
import java.util.ArrayList

public data class TorrentFile(
        val length:Long,
        val path:List<String>
)

public data class TorrentFiles(
        val name:String,
        val length:Long?,
        val files: List<TorrentFile> = Collections.emptyList())

public data class TorrentInfo(
        val pieceLength:Long,
        val hashes:List<String>,
        val files:TorrentFiles)

public data class Announce(
        val url:URL,
        val additional:List<URL> = Collections.emptyList())

public data class Torrent(
        val info:TorrentInfo,
        val announce:Announce,
        val created:Date?,
        val comment:String?,
        val createdBy:String?)