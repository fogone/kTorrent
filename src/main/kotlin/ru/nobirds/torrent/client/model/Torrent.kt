package ru.nobirds.torrent.client.model

import ru.nobirds.torrent.utils.equalsNullable
import java.util.*

public data class Torrent(
        val info:TorrentInfo,
        val announce:Announce,
        val created:Date? = null,
        val comment:String? = null,
        val createdBy:String? = null) {

    public fun equals(torrent:Torrent):Boolean {
        if(!torrent.info.equals(info)) return false
        if(!torrent.announce.equals(announce)) return false
        if(!torrent.created.equalsNullable(created)) return false
        if(!torrent.createdBy.equalsNullable(createdBy)) return false
        if(!torrent.comment.equalsNullable(comment)) return false

        return true
    }

}


