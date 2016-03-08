package ru.nobirds.torrent.parser

import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.model.Torrent
import ru.nobirds.torrent.client.model.Torrents
import ru.nobirds.torrent.utils.asString
import ru.nobirds.torrent.utils.copyTo

class TorrentParserImpl(val digest: DigestProvider) : TorrentParser {

    private val HASH_SIZE = 20

    override fun parse(source:BMap):Torrent = Torrents.createTorrent(digest) {
        created(source.getDate("creation date"))
        createdBy(source.getString("created by"))
        comment(source.getString("comment"))

        announce(source.getString("announce")!!) {
            val announces = source.getBList("announce-list")?.
                map { it as BList }?.flatMap { it }?.map { (it as BBytes).value.asString() }

            if(announces != null)
                for (url in announces) {
                    url(url)
                }
        }

        val info = source.getBMap("info")!!
        info(info.getLong("piece length")!!) {
            hashOf(Bencoder.encodeBType(info))

            hashes {
                val pieces = info.getBytes("pieces")!!
                val piecesCount = pieces.size / HASH_SIZE

                var position = 0

                repeat(piecesCount) {
                    hash(pieces.copyTo(ByteArray(HASH_SIZE), position))
                    position += HASH_SIZE
                }
            }

            files(info.getString("name")!!) {
                length(info.getLong("length"))
                val files = info.getBList("files")

                if(files != null)
                for (file in files.map { it as  BMap }) {
                    file(file.getLong("length")!!, *file.getStrings("path")!!.toTypedArray())
                }
            }
        }
    }

}