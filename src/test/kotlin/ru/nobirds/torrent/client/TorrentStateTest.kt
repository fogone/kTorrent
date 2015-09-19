package ru.nobirds.torrent.client

import org.junit.Assert
import org.junit.Test
import ru.nobirds.torrent.client.model.Torrents
import ru.nobirds.torrent.client.task.file.CompositeRandomAccessFile
import ru.nobirds.torrent.client.task.state.ChoppedState
import ru.nobirds.torrent.parser.TorrentSerializer
import ru.nobirds.torrent.utils.randomAccess
import java.io.File
import java.nio.file.Paths
import java.security.MessageDigest

public class TorrentStateTest() {

    @Test
    public fun test1() {
        //val directory = File("D://Torrents//Vikings - Season 1 (AlexFilm) WEB-DL 1080p").toPath()
        val directory = File("D:\\Torrents\\4R6").toPath()
        val torrent = Torrents.createTorrentForDirectory(DigestProvider { MessageDigest.getInstance("SHA-1") }, directory, 1024L * 1024L)

        System.out.println(TorrentSerializer().torrentToBMap(torrent).toString())
    }

    @Test
    public fun test2() {
        val digestProvider = DigestProvider { MessageDigest.getInstance("SHA-1") }

        val directory = Paths.get(ClassLoader.getSystemResource("torrent")!!.toURI())!!
        val torrent = Torrents.createTorrentForDirectory(digestProvider, directory, 4)

        val compositeFile = CompositeRandomAccessFile(
                arrayListOf(File(ClassLoader.getSystemResource("torrent/test.file")!!.toURI()).randomAccess("r"))
        )

        val state = ChoppedState(torrent.info, 2)

        val bitSet = digestProvider.checkHashes(torrent.info.pieceLength, torrent.info.hashes, compositeFile)

        state.done(bitSet)

        Assert.assertTrue(state.isDone())
        Assert.assertEquals(4, state.count)
        Assert.assertEquals(7, state.blocksCount)

        /*Assert.assertEquals("01", compositeFile.read(state.blockIndexToGlobalIndex(0, 0)).asString())
        Assert.assertEquals("AB", compositeFile.read(state.blockIndexToGlobalIndex(2, 1)).asString())
        Assert.assertEquals("C", compositeFile.read(state.blockIndexToGlobalIndex(3, 0)).asString())*/
    }


}