package ru.nobirds.torrent.parser

import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.client.model.Torrent
import java.io.InputStream

public interface TorrentParser {

    fun parse(source:InputStream):Torrent = parse(Bencoder.decodeBMap(source))

    fun parse(source:BMap):Torrent

}