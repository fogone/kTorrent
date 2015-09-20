package ru.nobirds.torrent.parser

import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.model.*
import ru.nobirds.torrent.utils.asString
import ru.nobirds.torrent.utils.nullOr
import java.util.*

@Deprecated("Use TorrentParserImpl")
public class OldTorrentParserImpl(val digest: DigestProvider) : TorrentParser {

    private val HASH_SIZE = 20

    public override fun parse(source:BMap):Torrent {
        val torrentInfo = parseTorrentInfo(source.getBMap("info")!!)

        val announce = parseAnnounce(source)

        val creationDate = source.getDate("creation date")
        val createdBy = source.getString("created by")
        val comment = source.getString("comment")

        return Torrent(
                info = torrentInfo,
                announce = announce,
                created = creationDate,
                createdBy = createdBy,
                comment = comment
        )
    }

    private fun parseAnnounce(map: BMap):Announce {

        val announces = map.getBList("announce-list").nullOr {
            map { it as BList }.flatMap { it }.map { (it as BBytes).value.asString() }
        }

        return Announce(
                url = map.getString("announce")!!,
                additional = announces ?: Collections.emptyList()
        )
    }

    private fun parseTorrentInfo(map: BMap):TorrentInfo {
        val infoBytes = Bencoder.encodeBType(map)

        val hash = digest.encode(infoBytes)

        val pieceLength = map.getLong("piece length")!!
        val pieces = map.getBytes("pieces")!!

        val piecesCount = pieces.size() / HASH_SIZE

        val hashes = splitHashes(pieces, piecesCount)

        val name = map.getString("name")!!
        val length = map.getLong("length")

        val files = TorrentFiles(
                name = name,
                length = length,
                files = parseFiles(map.getBList("files")!!.map { it as BMap })
        )

        return TorrentInfo(
                hash = hash,
                pieceLength = pieceLength,
                hashes = hashes,
                files = files
        )
    }

    private fun parseFiles(list:List<BMap>?):List<TorrentFile> {
        if(list == null)
            return Collections.emptyList()

        return list.map { parseFile(it) }
    }

    private fun parseFile(map: BMap):TorrentFile {
        val length = map.getLong("length")!!
        val path = map.getStrings("path")!!

        return TorrentFile(length, path)
    }

    private fun splitHashes(pieces:ByteArray, count:Int):List<ByteArray> {
        val list = ArrayList<ByteArray>(count)

        var position = 0

        repeat(count) {
            val hash = ByteArray(HASH_SIZE)
            System.arraycopy(pieces, position, hash, 0, HASH_SIZE)
            list.add(hash)
            position += HASH_SIZE
        }

        return list
    }

}