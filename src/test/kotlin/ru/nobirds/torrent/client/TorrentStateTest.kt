package ru.nobirds.torrent.client

import org.junit.Test
import ru.nobirds.torrent.client.task.TorrentState
import ru.nobirds.torrent.client.model.TorrentBuilder
import ru.nobirds.torrent.client.model.Torrents
import java.io.File
import java.nio.file.Paths
import java.net.URI
import ru.nobirds.torrent.client.parser.TorrentSerializer
import java.io.FileOutputStream
import ru.nobirds.torrent.bencode.BTypeFormatter
import java.io.OutputStreamWriter

public class TorrentStateTest() {

    Test
    public fun test1() {
        //val directory = File("D://Torrents//Vikings - Season 1 (AlexFilm) WEB-DL 1080p").toPath()
        val directory = File("D:\\Torrents\\4R6").toPath()
        val torrent = Torrents.createTorrentForDirectory(directory, 1024L * 1024L)

        val writer = OutputStreamWriter(System.out)
        BTypeFormatter(writer).format(TorrentSerializer().torrentToBMap(torrent))
        writer.flush()

        //TorrentSerializer().serialize(torrent, FileOutputStream("D:\\Torrents\\4R6\\tmp.torrent"))
    }


}