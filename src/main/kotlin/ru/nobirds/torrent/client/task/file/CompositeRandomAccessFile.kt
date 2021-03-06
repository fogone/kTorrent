package ru.nobirds.torrent.client.task.file

import ru.nobirds.torrent.client.task.state.GlobalBlockPositionAndSize
import ru.nobirds.torrent.utils.closeQuietly
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutput
import java.io.DataOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile

class InputImplementer(val file:CompositeRandomAccessFile) : InputStream() {
    override fun read(): Int = file.read()

    override fun read(b: ByteArray, off: Int, len: Int): Int = file.read(b, off, len)
}

class OutputImplementer(val file:CompositeRandomAccessFile) : OutputStream() {
    override fun write(b: Int) {
        file.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        file.write(b, off, len)
    }
}

class CompositeRandomAccessFile(val files:List<RandomAccessFile>) {

    private val input:DataInput = DataInputStream(InputImplementer(this))
    private val output:DataOutput = DataOutputStream(OutputImplementer(this))

    private var index:Int = 0

    private val current:RandomAccessFile
        get() = files[index]

    private val lengths = files.map { it.length() }

    val length:Long = lengths.reduce { it, length -> it + length }

    fun seek(position:Long) {
        var pos = 0L
        var index = 0

        for (file in files) {
            if(position in pos..pos+file.length()) {
                this.index = index
                file.seek(position - pos)
                return
            } else {
                pos += file.length()
            }
            index++
        }

        throw IllegalStateException()
    }

    fun write(b:ByteArray) {
        write(b, 0, b.size)
    }

    fun write(b:ByteArray, off:Int, len:Int) {
        val toWrite = len - off

        val currentPosition = current.filePointer
        val currentLength = current.length()

        if(currentPosition + toWrite <= currentLength) {
            current.write(b, off, len)
        } else { // todo: while
            val left = (currentLength - currentPosition).toInt()
            current.write(b, off, left)
            next()
            current.write(b, off + left, len - left)
        }
    }

    fun write(b:Int) {
        if(current.filePointer == current.length()) {
            if (index == files.size - 1) {
                throw EOFException()
            } else {
                next()
            }
        }

        current.write(b)
    }

    fun read(b:ByteArray, off:Int, len:Int):Int {
        val toRead = len - off

        val currentPosition = current.filePointer
        val currentLength = current.length()

        if(currentPosition + toRead <= currentLength) {
            return current.read(b, off, len)
        } else { // todo: while
            val left = (currentLength - currentPosition).toInt()
            var read = current.read(b, off, left)
            next()
            read += current.read(b, off + left, len - left)
            return read
        }
    }

    fun read():Int {
        var value = current.read()

        if(value == -1) {
            if(index == files.size -1)
                return -1
            else {
                next()
                value = current.read()
            }
        }

        return value
    }

    fun read(bytes:ByteArray) {
        input.readFully(bytes)
    }

    fun read(index: GlobalBlockPositionAndSize, buffer:ByteArray = ByteArray(index.length)):ByteArray {
        seek(index.begin.toLong())
        input.readFully(buffer)
        return buffer
    }

    private fun next() {
        index++
        current.seek(0)
    }

    fun close() {
        for (file in files) {
            file.closeQuietly()
        }
    }
}