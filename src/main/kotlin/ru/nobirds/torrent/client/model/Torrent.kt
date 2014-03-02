package ru.nobirds.torrent.client.model

import java.util.Date
import java.net.URL
import java.util.Collections
import java.util.ArrayList
import java.math.BigInteger

public data class Torrent(
        val info:TorrentInfo,
        val announce:Announce,
        val created:Date?,
        val comment:String?,
        val createdBy:String?)


