package ru.nobirds.torrent.client.parser

import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.bencode.BMapHelper
import ru.nobirds.torrent.client.model.Torrent
import ru.nobirds.torrent.client.model.Torrents
import ru.nobirds.torrent.nullOr
import ru.nobirds.torrent.asString

public class TorrentParserImpl : TorrentParser {

    private val HASH_SIZE = 20

    public override fun parse(source:BMap):Torrent = Torrents.createTorrent {

        val map = BMapHelper(source)

        created(map.getDate("creation date"))
        createdBy(map.getString("created by"))
        comment(map.getString("comment"))

        announce(map.getString("announce")!!) {
            val announces = map.getBList("announce-list").nullOr {
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