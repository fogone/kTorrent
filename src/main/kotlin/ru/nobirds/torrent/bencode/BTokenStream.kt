package ru.nobirds.torrent.bencode

import io.netty.buffer.ByteBuf
import java.io.InputStream

interface BTokenStream {

    fun next():Int

    fun hasNext():Boolean

    fun nextBytes(count:Int):ByteArray

    fun current():Int

    fun currentChar():Char = current().toChar()

    fun position():Long

    fun processBType():BType {
        val value = createBType(currentChar())
        value.process(this)
        return value
    }

    private fun createBType(char:Char): BType {
        return when(char) {
            'i', 'I' -> BNumber()
            'l', 'L' -> BList()
            'd', 'D' -> BMap()
            in '0'..'9' -> BBytes()
            else -> throw IllegalCharacterException(char, position())
        }
    }
}

class BTokenStreamImpl(val reader:ByteReader) : BTokenStream {

    private var current:Int = -1

    private var next:Int = reader.read()

    private var position:Long = -1L

    private val emptyBytes = ByteArray(0)

    override fun next():Int {
        this.current = this.next
        this.next = reader.read()
        position++
        return current()
    }

    override fun hasNext(): Boolean = next > 0

    override fun nextBytes(count:Int):ByteArray {
        if(count == 0)
            return emptyBytes

        val result = ByteArray(count)
        result[0] = next.toByte()
        val read = reader.read(result, 1, count - 1)

        if(read != count -1)
            throw IllegalStateException()

        this.current = result[count-1].toInt()
        this.next = reader.read()

        position += read + 1

        return result
    }

    override fun current():Int = current

    override fun position():Long = position

}

interface ByteReader {

    fun read():Int

    fun read(buffer:ByteArray, index:Int, count:Int):Int

}

class StreamByteReader(val stream:InputStream) : ByteReader {

    override fun read(): Int = stream.read()

    override fun read(buffer: ByteArray, index: Int, count: Int): Int {
        return stream.read(buffer, index, count)
    }

}

class BufferByteReader(val buffer:ByteBuf) : ByteReader {

    override fun read(): Int = if(buffer.readableBytes() < 1) -1 else buffer.readByte().toInt()

    override fun read(buffer: ByteArray, index: Int, count: Int): Int {
        val realCount = if(this.buffer.readableBytes() < count) this.buffer.readableBytes() else count

        this.buffer.readBytes(buffer, index, realCount)

        return realCount
    }

}