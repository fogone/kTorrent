package ru.nobirds.torrent.client.parser

import java.io.InputStream
import ru.nobirds.torrent.bencode.BMap
import java.util.ArrayList
import java.util.Collections
import java.net.URL


import ru.nobirds.torrent.client.Sha1Provider
import ru.nobirds.torrent.nullOr
import ru.nobirds.torrent.asString
import ru.nobirds.torrent.toHexString
import ru.nobirds.torrent.client.model.Torrent
import ru.nobirds.torrent.client.model.Announce
import ru.nobirds.torrent.client.model.TorrentInfo
import ru.nobirds.torrent.client.model.TorrentFiles
import ru.nobirds.torrent.client.model.TorrentFile
import ru.nobirds.torrent.bencode.BKeyValuePair
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.client.model.TorrentBuilder
import ru.nobirds.torrent.client.model.Torrents

public class TorrentParserImpl : TorrentParser {

    private val HASH_SIZE = 20

    public override fun parse(source:BMap):Torrent = Torrents.createTorrent {

        val map = MapHelper(source)

        created(map.getDate("creation date"))
        createdBy(map.getString("created by"))
        comment(map.getString("comment"))

        announce(map.getString("announce")!!) {
            val announces = map.getList("announce-list").nullOr {
                map { it as BList }.flatMap { it }.map { (it as BBytes).value.asString() }
            }

            if(announces != null)
                for (url in announces) {
                    url(url)
                }
        }

        val info = map.getMap("info")!!
        info(info.getLong("piece length")!!) {
            hashOf(Bencoder.encodeBType(info.map))

            hashes {
                val pieces = info.getBytes("pieces")!!
                val piecesCount = pieces.size / HASH_SIZE

                var position = 0

                piecesCount.times {
                    val hash = ByteArray(HASH_SIZE)
                    System.arraycopy(pieces, position, hash, 0, HASH_SIZE)

                    hash(hash)

                    position += HASH_SIZE
                }
            }

            files(info.getString("name")!!) {
                length(info.getLong("length"))
                val files = info.getListOfMaps("files")

                if(files != null)
                for (file in files) {
                    file(file.getLong("length")!!, *file.getStrings("path")!!.copyToArray())
                }
            }
        }
    }

}