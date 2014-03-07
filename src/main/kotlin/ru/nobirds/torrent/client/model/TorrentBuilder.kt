package ru.nobirds.torrent.client.model

import java.util.Date
import kotlin.properties.Delegates
import java.util.ArrayList
import ru.nobirds.torrent.toHexString
import ru.nobirds.torrent.client.Sha1Provider

public class TorrentFilesBuilder(val name:String) {

    private var length:Long? = null

    private val files = ArrayList<TorrentFile>()

    public fun file(length:Long, vararg path:String) {
        files.add(TorrentFile(length, path.toList()))
    }

    public fun length(length:Long?) {
        this.length = length
    }

    public fun build():TorrentFiles = TorrentFiles(
            name, length, files
    )

}

public class HashesBuilder {

    private val hashes:MutableList<ByteArray> = ArrayList()

    public fun hash(hash:ByteArray) {
        hashes.add(hash)
    }

    public fun hashOf(body:ByteArray) {
        hashes.add(Sha1Provider.encodeAsBytes(body))
    }

    public fun build():MutableList<ByteArray> = hashes
}

public class TorrentInfoBuilder(var pieceLength:Long) {

    private var hash:ByteArray by Delegates.notNull()

    private var hashes:MutableList<ByteArray> = ArrayList()
    private var files:TorrentFiles by Delegates.notNull()

    public fun hashOf(bytes:ByteArray) {
        hash = Sha1Provider.encodeAsBytes(bytes)
    }

    public fun hashes(block:HashesBuilder.()->Unit) {
        val builder = HashesBuilder()
        builder.block()
        hashes.addAll(builder.build())
    }

    public fun files(name:String, block:TorrentFilesBuilder.()->Unit) {
        val builder = TorrentFilesBuilder(name)
        builder.block()
        files = builder.build()
    }

    public fun build():TorrentInfo = TorrentInfo(
            hash, pieceLength, hashes, files
    )

}

public class AnnounceBuilder(val url:String) {

    private val additional = ArrayList<String>()

    public fun url(url:String) {
        additional.add(url)
    }

    public fun build():Announce = Announce(
            url, additional
    )

}

public class TorrentBuilder {

    private var info:TorrentInfo by Delegates.notNull()
    private var announce:Announce by Delegates.notNull()
    private var created:Date? = null
    private var comment:String? = null
    private var createdBy:String? = null

    public fun info(pieceLength:Long, block:TorrentInfoBuilder.()->Unit) {
        val builder = TorrentInfoBuilder(pieceLength)
        builder.block()
        info = builder.build()
    }

    public fun announce(url:String, block:AnnounceBuilder.()->Unit) {
        val builder = AnnounceBuilder(url)
        builder.block()
        announce = builder.build()
    }

    public fun created(created:Date?) {
        this.created = created
    }

    public fun createdBy(createdBy:String?) {
        this.createdBy = createdBy
    }

    public fun comment(comment:String?) {
        this.comment = comment
    }

    public fun build():Torrent = Torrent(
            info, announce, created, comment, createdBy
    )

}

public object Torrents {

    public fun createTorrent(block:TorrentBuilder.()->Unit):Torrent {
        val builder = TorrentBuilder()
        builder.block()
        return builder.build()
    }

}