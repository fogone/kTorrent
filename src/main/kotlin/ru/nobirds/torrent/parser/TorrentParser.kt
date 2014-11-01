package ru.nobirds.torrent.parser

import java.io.InputStream
import ru.nobirds.torrent.client.model.Torrent
import ru.nobirds.torrent.bencode.BMap

public trait TorrentParser {

    fun parse(source:InputStream):Torrent = parse(Bencoder.decodeBMap(source))

    fun parse(source:BMap):Torrent

}