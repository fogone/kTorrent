package ru.nobirds.torrent.client.parser

import ru.nobirds.torrent.client.model.Torrent
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.client.model.Announce
import java.util.Collections
import ru.nobirds.torrent.client.model.TorrentInfo
import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.model.TorrentFiles
import ru.nobirds.torrent.client.model.TorrentFile
import java.util.ArrayList
import ru.nobirds.torrent.bencode.BMapHelper
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.utils.nullOr
import ru.nobirds.torrent.utils.asString

deprecated("Use TorrentParserImpl")
public class OldTorrentParserImpl(val digest: DigestProvider) : TorrentParser {

    private val HASH_SIZE = 20

    public override fun parse(source:BMap):Torrent {
        val map = BMapHelper(source)

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

    private fun parseAnnounce(map: BMapHelper):Announce {

        val announces = map.getBList("announce-list").nullOr {
            map { it as BList }.flatMap { it }.map { (it as BBytes).value.asString() }
        }

        return Announce(
                url = map.getString("announce")!!,
                additional = announces ?: Collections.emptyList()
        )
    }

    private fun parseTorrentInfo(map: BMapHelper):TorrentInfo {
        val infoBytes = Bencoder.encodeBType(map.map)

        val hash = digest.encode(infoBytes)

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

    private fun parseFiles(list:List<BMapHelper>?):List<TorrentFile> {
        if(list == null)
            return Collections.emptyList()

        return list.map { parseFile(it) }
    }

    private fun parseFile(map: BMapHelper):TorrentFile {
        val length = map.getLong("length")!!
        val path = map.getStrings("path")!!

        return TorrentFile(length, path)
    }

    private fun splitHashes(pieces:ByteArray, count:Int):List<ByteArray> {
        val list = ArrayList<ByteArray>(count)

        var position = 0

        count.times {
            val hash = ByteArray(HASH_SIZE)
            System.arraycopy(pieces, position, hash, 0, HASH_SIZE)
            list.add(hash)
            position += HASH_SIZE
        }

        return list
    }

}