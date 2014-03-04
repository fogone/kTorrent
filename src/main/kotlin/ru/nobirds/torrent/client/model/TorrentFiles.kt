package ru.nobirds.torrent.client.model

import java.util.Collections


public data class TorrentFiles(
        val name:String,
        val length:Long?,
        val files: List<TorrentFile> = Collections.emptyList()) {

    public val totalLength:Long = files
            .map { it.length }
            .reduce { total, it -> it + total  }

}

