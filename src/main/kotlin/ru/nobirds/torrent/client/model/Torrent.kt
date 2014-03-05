package ru.nobirds.torrent.client.model

import java.util.Date

public data class Torrent(
        val info:TorrentInfo,
        val announce:Announce,
        val created:Date?,
        val comment:String?,
        val createdBy:String?)


