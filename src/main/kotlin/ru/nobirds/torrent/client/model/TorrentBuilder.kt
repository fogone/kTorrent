package ru.nobirds.torrent.client.model

import ru.nobirds.torrent.client.DigestProvider
import ru.nobirds.torrent.client.task.file.CompositeRandomAccessFile
import ru.nobirds.torrent.parser.TorrentSerializer
import ru.nobirds.torrent.utils.randomAccess
import java.io.File
import java.nio.file.Path
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import kotlin.properties.Delegates

class TorrentFilesBuilder(val name:String) {

    private var length:Long? = null

    private val files = ArrayList<TorrentFile>()

    fun file(length:Long, vararg path:String) {
        files.add(TorrentFile(length, path.toList()))
    }

    fun file(length:Long, path:List<String>) {
        files.add(TorrentFile(length, path))
    }

    fun length(length:Long?) {
        this.length = length
    }

    fun build():TorrentFiles = TorrentFiles(
            name, length, files
    )

}

class HashesBuilder(val digest: DigestProvider) {

    private val hashes:MutableList<ByteArray> = ArrayList()

    fun hash(hash:ByteArray) {
        hashes.add(hash)
    }

    fun hashOf(body:ByteArray) {
        hashes.add(digest.encode(body))
    }

    fun build():MutableList<ByteArray> = hashes
}

class TorrentInfoBuilder(val digest: DigestProvider, val pieceLength:Long) {

    private var hash:ByteArray? = null

    private var hashes:MutableList<ByteArray> = ArrayList()
    private var files:TorrentFiles by Delegates.notNull()

    fun hashOf(bytes:ByteArray) {
        this.hash = digest.encode(bytes)
    }

    fun hashes(hashes:List<ByteArray>) {
        this.hashes.addAll(hashes)
    }

    fun hashes(block:HashesBuilder.()->Unit) {
        val builder = HashesBuilder(digest)
        builder.block()
        hashes.addAll(builder.build())
    }

    fun files(name:String, block:TorrentFilesBuilder.()->Unit) {
        val builder = TorrentFilesBuilder(name)
        builder.block()
        files = builder.build()
    }

    fun build():TorrentInfo {
        val info = TorrentInfo(pieceLength, hashes, files, hash)

        if(hash == null)
            info.hash = digest.encode(TorrentSerializer().serialize(info))

        return info
    }

}

class AnnounceBuilder(val url:String) {

    private val additional = ArrayList<String>()

    fun url(url:String) {
        additional.add(url)
    }

    fun build():Announce = Announce(
            url, additional
    )

}

class TorrentBuilder(val digest: DigestProvider) {

    private var info:TorrentInfo by Delegates.notNull()
    private var announce:Announce by Delegates.notNull()
    private var created:Date? = null
    private var comment:String? = null
    private var createdBy:String? = null

    fun info(pieceLength:Long, block:TorrentInfoBuilder.()->Unit) {
        val builder = TorrentInfoBuilder(digest, pieceLength)
        builder.block()
        info = builder.build()
    }

    fun announce(url:String, block:AnnounceBuilder.()->Unit = {}) {
        val builder = AnnounceBuilder(url)
        builder.block()
        announce = builder.build()
    }

    fun created(created:Date?) {
        this.created = created
    }

    fun createdBy(createdBy:String?) {
        this.createdBy = createdBy
    }

    fun comment(comment:String?) {
        this.comment = comment
    }

    fun build():Torrent = Torrent(
            info, announce, created, comment, createdBy
    )

}

object Torrents {

    fun createTorrent(digest: DigestProvider, block:TorrentBuilder.()->Unit):Torrent {
        val builder = TorrentBuilder(digest)
        builder.block()
        return builder.build()
    }

    fun createTorrentForDirectory(digest: DigestProvider, directory:Path, pieceLength:Long = 16L * 1024L):Torrent = createTorrent(digest) {

        created(Date())
        createdBy("kTorrent client 0.1.alfa")

        announce("http://localhost:9999/")

        info(pieceLength) {

            val root = directory.toFile()

            val files = fetchFiles(root)

            val rndAccessFile = CompositeRandomAccessFile(files.map { it.randomAccess("r") })

            val hashes = digest.calculateHashes(rndAccessFile, pieceLength)

            hashes(hashes)

            files(root.name) {
                for (file in files) {
                    file(file.length(), directory.relativize(file.toPath()!!).map { it.toString() })
                }
            }

            rndAccessFile.close()
        }
    }

    private fun fetchFiles(root:File):List<File> = if(root.isDirectory) root.listFiles()!!.flatMap { fetchFiles(it) }
                                                    else Collections.singletonList(root)

}