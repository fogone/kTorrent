package ru.nobirds.torrent.client.task.file

import java.io.RandomAccessFile
import java.io.DataInput
import java.io.DataOutput
import java.io.DataInputStream
import java.io.InputStream
import java.io.DataOutputStream
import java.io.OutputStream
import java.io.EOFException

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

public class CompositeRandomAccessFile(val files:List<RandomAccessFile>) {

    public val input:DataInput = DataInputStream(InputImplementer(this))
    public val output:DataOutput = DataOutputStream(OutputImplementer(this))

    private var index:Int = 0

    private val current:RandomAccessFile
        get() = files[index]

    private val lengths = files.map { it.length() }

    public val length:Long = lengths.reduce { it, length -> it + length }

    public fun seek(position:Long) {
        var pos = 0L
        files.forEachWithIndex { index, file ->
            if(position in pos..pos+file.length()) {
                this.index = index
                file.seek(position - pos)
            } else {
                pos += file.length()
            }
        }

        throw IllegalStateException()
    }

    public fun write(b:ByteArray, off:Int, len:Int) {
        //current.write(b, off, len)

        val toWrite = len - off

        val currentPosition = current.getFilePointer()
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

    public fun write(b:Int) {
        if(current.getFilePointer() == current.length()) {
            if(index == files.size-1)
                throw EOFException()
            else
                next()
        }

        current.write(b)
    }

    public fun read(b:ByteArray, off:Int, len:Int):Int {
        val toRead = len - off

        val currentPosition = current.getFilePointer()
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

    public fun read():Int {
        var value = current.read()

        if(value == -1) {
            if(index == files.size-1)
                return -1
            else {
                next()
                value = current.read()
            }
        }

        return value
    }

    private fun next() {
        index++
        current.seek(0)
    }

}