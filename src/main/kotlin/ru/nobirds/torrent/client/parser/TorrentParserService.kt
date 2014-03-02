package ru.nobirds.torrent.client.parser

import java.io.InputStream
import ru.nobirds.torrent.bencode.BTokenInputStream
import ru.nobirds.torrent.bencode.BMap
import java.math.BigInteger
import java.util.Date
import java.util.ArrayList
import java.util.Collections
import java.net.URL
import org.springframework.stereotype.Service as service
import org.springframework.beans.factory.annotation.Autowired
import ru.nobirds.torrent.client.Sha1Service
import ru.nobirds.torrent.nullOr
import ru.nobirds.torrent.asString
import ru.nobirds.torrent.toHexString
import ru.nobirds.torrent.client.model.Torrent
import ru.nobirds.torrent.client.model.Announce
import ru.nobirds.torrent.client.model.TorrentInfo
import ru.nobirds.torrent.client.model.TorrentFiles
import ru.nobirds.torrent.client.model.TorrentFile

public service class TorrentParserService() {

    private Autowired var bencoderService:BencoderService? = null

    private Autowired var sha1Service:Sha1Service? = null

    private val HASH_SIZE = 20

    public fun parse(source:Map<String, Any>):Torrent {
        val map = MapHelper(source)

        val torrentInfo = parseTorrentInfo(map.getMap("info")!!)

        val announce = parseAnnounce(map)

        val creationDate = map.getDate("creation date")
        val createdBy = map.getString("created by")
        val comment = map.getString("comment")

        return Torrent(
                info = torrentInfo,
                announce = announce,
                created = creationDate,
                createdBy = createdBy,
                comment = comment
        )
    }

    public fun parse(stream:InputStream):Torrent = parse(bencoderService!!.decode(stream))

    private fun parseAnnounce(map:MapHelper):Announce {

        val announces = map.getList("announce-list").nullOr {
            map { it as List<ByteArray> }.flatMap { it }.map { URL(it.asString()) }
        }

        return Announce(
                url = URL(map.getString("announce")!!),
                additional = announces ?: Collections.emptyList()
        )
    }

    private fun parseTorrentInfo(map:MapHelper):TorrentInfo {

        val hash = sha1Service!!.encode(bencoderService!!.encode(map.map))

        val pieceLength = map.getLong("piece length")!!
        val pieces = map.getBytes("pieces")!!

        val piecesCount = pieces.size / HASH_SIZE

        val hashes = splitHashes(pieces, piecesCount)

        val name = map.getString("name")!!
        val length = map.getLong("length")

        val files = TorrentFiles(
                name = name,
                length = length,
                files = parseFiles(map.getListOfMaps("files"))
        )

        return TorrentInfo(
                hash = hash,
                pieceLength = pieceLength,
                hashes = hashes,
                files = files
        )
    }

    private fun parseFiles(list:List<MapHelper>?):List<TorrentFile> {
        if(list == null)
            return Collections.emptyList()

        return list.map { parseFile(it) }
    }

    private fun parseFile(map:MapHelper):TorrentFile {
        val length = map.getLong("length")!!
        val path = map.getStrings("path")!!

        return TorrentFile(length, path)
    }

    private fun splitHashes(pieces:ByteArray, count:Int):List<String> {
        val list = ArrayList<String>(count)

        var position = 0

        count.times {
            val hash = ByteArray(HASH_SIZE)
            System.arraycopy(pieces, position, hash, 0, HASH_SIZE)
            list.add(hash.toHexString())
            position += HASH_SIZE
        }

        return list
    }

}