package ru.nobirds.torrent.client.model

import ru.nobirds.torrent.utils.equalsNullable
import java.util.ArrayList

data class TorrentFiles(
        val name:String,
        val length:Long? = null,
        val files: List<TorrentFile> = ArrayList()) {

    val totalLength:Long = files
            .fold(0L) { total, it -> total + it.length.toLong() }

    fun equals(other:TorrentFiles):Boolean {
        if(!other.name.equals(name)) return false
        if(!other.length.equalsNullable(length)) return false
        if(!other.files.equals(files)) return false

        return true
    }

}

