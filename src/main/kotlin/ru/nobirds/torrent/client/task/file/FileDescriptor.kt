package ru.nobirds.torrent.client.task.file

import ru.nobirds.torrent.client.model.TorrentFile
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path

class FileDescriptor(val parent:Path, val torrentFile:TorrentFile) {

    private val file = createFile(torrentFile.path)

    val randomAccessFile:RandomAccessFile = createRandomAccessFile()

    val length:Long
        get() = randomAccessFile.length()

    private fun createRandomAccessFile():RandomAccessFile {
        val randomAccessFile = RandomAccessFile(file.toFile(), "rw")
        randomAccessFile.setLength(torrentFile.length)
        return randomAccessFile
    }

    private fun createFile(path:List<String>):Path {
        var result = parent

        for (item in path) {
            result = result.resolve(item)
        }

        val directory = result.parent

        if(directory != null && !Files.exists(directory))
            Files.createDirectories(directory)

        return result
    }

}