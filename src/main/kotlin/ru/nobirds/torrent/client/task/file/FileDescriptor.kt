package ru.nobirds.torrent.client.task

import java.nio.file.Files
import java.nio.file.Path
import java.io.RandomAccessFile
import ru.nobirds.torrent.client.model.TorrentFile

public class FileDescriptor(val parent:Path, val torrentFile:TorrentFile) {

    private val file = createFile(torrentFile.path)

    val randomAccessFile:RandomAccessFile = createRandomAccessFile()

    public val length:Long
        get() = randomAccessFile.length()

    private fun createRandomAccessFile():RandomAccessFile {
        val randomAccessFile = RandomAccessFile(file.toFile(), "rw")
        randomAccessFile.setLength(torrentFile.length)
        return randomAccessFile
    }

    private fun createFile(path:List<String>):Path {
        var result = parent
        for (item in path) {
            result = result.resolve(item)!!
        }

        val directory = result.getParent()

        if(directory != null && !Files.exists(directory))
            Files.createDirectories(result.getParent()!!)

        return result
    }

}