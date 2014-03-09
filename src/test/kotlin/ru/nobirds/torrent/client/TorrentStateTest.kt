package ru.nobirds.torrent.client

import org.junit.Test
import ru.nobirds.torrent.client.task.state.TorrentState
import ru.nobirds.torrent.client.model.TorrentBuilder
import ru.nobirds.torrent.client.model.Torrents
import java.io.File
import java.nio.file.Paths
import java.net.URI
import ru.nobirds.torrent.client.parser.TorrentSerializer
import java.io.FileOutputStream
import ru.nobirds.torrent.bencode.BTypeFormatter
import java.io.OutputStreamWriter
import org.junit.Assert
import java.util.BitSet
import ru.nobirds.torrent.client.task.file.CompositeRandomAccessFile
import ru.nobirds.torrent.randomAccess
import ru.nobirds.torrent.asString

public class TorrentStateTest() {

    Test
    public fun test1() {
        //val directory = File("D://Torrents//Vikings - Season 1 (AlexFilm) WEB-DL 1080p").toPath()
        val directory = File("D:\\Torrents\\4R6").toPath()
        val torrent = Torrents.createTorrentForDirectory(directory, 1024L * 1024L)

        System.out.println(TorrentSerializer().torrentToBMap(torrent).toString())
    }

    Test
    public fun test2() {
        val directory = Paths.get(ClassLoader.getSystemResource("torrent")!!.toURI())!!
        val torrent = Torrents.createTorrentForDirectory(directory, 4)

        val compositeFile = CompositeRandomAccessFile(
                arrayListOf(File(ClassLoader.getSystemResource("torrent/test.file")!!.toURI()).randomAccess("r"))
        )

        val state = TorrentState(torrent.info, 2)

        val bitSet = Sha1Provider.checkHashes(torrent.info.pieceLength, torrent.info.hashes, compositeFile)

        state.done(bitSet)

        Assert.assertTrue(state.isDone())
        Assert.assertEquals(4, state.piecesCount)
        Assert.assertEquals(7, state.blocksCount)

        Assert.assertEquals("01", compositeFile.read(state.blockIndexToGlobalIndex(0, 0)).asString())
        Assert.assertEquals("AB", compositeFile.read(state.blockIndexToGlobalIndex(2, 1)).asString())
        Assert.assertEquals("C", compositeFile.read(state.blockIndexToGlobalIndex(3, 0)).asString())
    }


}