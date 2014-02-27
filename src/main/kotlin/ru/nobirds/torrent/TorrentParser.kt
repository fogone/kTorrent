package ru.nobirds.torrent

import java.io.InputStream
import ru.nobirds.torrent.bencode.BTokenInputStream
import ru.nobirds.torrent.bencode.BMap
import java.math.BigInteger
import java.util.Date
import java.util.ArrayList
import java.util.Collections
import ru.nobirds.torrent.bencode.Bencoder
import java.net.URL

class MapHelper(val map:Map<String, Any>) {

    fun get<T>(key:String, cast:(Any)->T):T? {
        val value = map[key]
        return if(value != null) cast(value)
            else null
    }

    fun getMap(key:String):MapHelper? = get(key) { MapHelper(it as Map<String, Any>) }

    fun getList(key:String):List<Any>? = get(key) { it as List<Any> }

    fun getListOfMaps(key:String):List<MapHelper>? = getList(key).nullOr { map { MapHelper(it as Map<String, Any>) } }

    fun getStrings(key:String):List<String>? = getList(key).nullOr { map { (it as ByteArray).asString() } }

    fun getBytes(key:String):ByteArray? = get(key) { it as ByteArray }

    fun getString(key:String):String? = getBytes(key).nullOr { (this as ByteArray).asString() }

    fun getBigInteger(key:String):BigInteger? = get(key) { it as BigInteger }

    fun getLong(key:String):Long? = getBigInteger(key).nullOr { longValue() }

    fun getDate(key:String):Date? = getLong(key).nullOr { Date(this) }

}

public class TorrentParser() {

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

    public fun parse(stream:InputStream):Torrent = parse(Bencoder.decode(stream))

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
            list.add(BigInteger(1, hash).toString(16))
            position += HASH_SIZE
        }

        return list
    }

}