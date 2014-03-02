package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.model.Torrent
import java.io.RandomAccessFile
import java.util.HashMap
import java.net.URL
import ru.nobirds.torrent.client.Peer
import java.util.HashSet
import ru.nobirds.torrent.client.model.TorrentFile
import java.io.File
import ru.nobirds.torrent.toMap

public class TorrentFileHandler(val parent:File, val torrentFile:TorrentFile) {

    private val file = createFile(torrentFile.path)
    private val randomAccessFile:RandomAccessFile = createRandomAccessFile()

    val name:String
        get() = file.path

    private fun createRandomAccessFile():RandomAccessFile {
        val randomAccessFile = RandomAccessFile(file, "rw")
        randomAccessFile.setLength(torrentFile.length)
        return randomAccessFile
    }

    private fun createFile(path:List<String>):File {
        var file = parent
        for (item in path) {
            file = File(file, item)
        }
        //file.mkdirs()
        return file
    }


}

public class TorrentTask(val directory:File, val torrent:Torrent) {

    private var state:TaskState = TaskState.stopped

    val uploadStatistics = TrafficStatistics()

    val downloadStatistics = TrafficStatistics()

    val files:Map<String, TorrentFileHandler> = createFiles()

    val peers = HashMap<URL, Set<Peer>>()

    private fun createFiles():Map<String, TorrentFileHandler> {
        val files = torrent.info.files

        val parent = File(directory, files.name)

//        if(!parent.mkdirs())
//            throw IllegalArgumentException("Can't access file ${parent.getAbsolutePath()}")

        return files.files.map { TorrentFileHandler(parent, it) }.toMap { it.name }
    }

    public fun updatePeers(url:URL, peers:List<Peer>) {
        this.peers[url] = HashSet(peers)
    }

}