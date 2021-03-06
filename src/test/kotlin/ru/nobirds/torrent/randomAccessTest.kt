package ru.nobirds.torrent

import org.junit.Assert
import org.junit.Test
import ru.nobirds.torrent.client.task.file.CompositeRandomAccessFile
import java.io.RandomAccessFile
import java.nio.file.Paths

class RandomAccessTest {

    @Test
    fun test1() {
        val file = CompositeRandomAccessFile(
                arrayOf("file1.txt", "file2.txt").map {
                    RandomAccessFile(Paths.get(ClassLoader.getSystemResource(it)!!.toURI())!!.toFile(), "rw")
                }
        )

        val byteArray = ByteArray(file.length.toInt())
        file.read(byteArray)
        val str = String(byteArray)
        Assert.assertEquals("hello worldthis is me", str)
    }

}