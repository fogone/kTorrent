package ru.nobirds.torrent.bencode

import java.io.InputStream

public class BTokenInputStream(val stream:InputStream) {

    private var current:Int = -1
    private var next:Int = stream.read()

    public fun hasNext():Boolean = next >= 0

    private var position:Long = -1L

    public fun next():Int {
        this.current = this.next
        this.next = stream.read()
        position++
        return current()
    }

    public fun nextBytes(count:Int):ByteArray {
        val result = ByteArray(count)
        result[0] = next.toByte()
        val read = stream.read(result, 1, count - 1)

        if(read != count -1)
            throw IllegalStateException()

        this.current = result[count-1].toInt()
        this.next = stream.read()

        position += read+1

        return result
    }

    public fun current():Int = current

    public fun position():Long = position

    public fun currentChar():Char = current.toChar()

    public fun processBType():BType<out Any> {
        val value = createBType(currentChar())
        value.process(this)
        return value
    }

    private fun createBType(char:Char): BType<out Any> {
        return when(char) {
            'i' -> BNumber()
            'l' -> BList()
            'd' -> BMap()
            in '0'..'9' -> BBytes()
            else -> throw IllegalArgumentException("Illegal character [${char}] ${char.toInt()}")
        }
    }
}