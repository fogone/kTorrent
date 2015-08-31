package ru.nobirds.torrent

import org.junit.Test
import ru.nobirds.torrent.client.task.file.CompositeRandomAccessFile
import java.io.RandomAccessFile
import java.nio.file.Paths
import org.junit.Assert

public class RandomAccessTest {


    Test
    public fun test1() {
        val file = CompositeRandomAccessFile(
                arrayOf("file1.txt", "file2.txt").map {
                    RandomAccessFile(Paths.get(ClassLoader.getSystemResource(it)!!.toURI())!!.toFile(), "rw")
                }
        )

        val byteArray = ByteArray(file.length.toInt())
        file.input.readFully(byteArray)
        val str = String(byteArray, "UTF-8")
        Assert.assertEquals("hello worldthis is me", str)
    }

}