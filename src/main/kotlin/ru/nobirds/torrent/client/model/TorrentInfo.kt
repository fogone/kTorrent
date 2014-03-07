package ru.nobirds.torrent.client.model

import java.util.ArrayList
import ru.nobirds.torrent.equalsArray
import java.util.Collections
import ru.nobirds.torrent.equalsList

public data class TorrentInfo(
        val pieceLength:Long,
        val hashes:List<ByteArray> = ArrayList(),
        val files:TorrentFiles,
        var hash:ByteArray? = null) {

    val pieceCount:Int
        get() = hashes.size

    public fun equals(info:TorrentInfo):Boolean {
        if(!info.pieceLength.equals(pieceLength)) return false
        if(!info.hashes.equalsList(hashes)) return false
        if(!info.files.equals(files)) return false

        return true
    }

}
