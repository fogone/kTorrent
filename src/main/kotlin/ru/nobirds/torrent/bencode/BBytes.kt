package ru.nobirds.torrent.bencode

import ru.nobirds.torrent.utils.asString

public class BBytes() : AbstractBType(), BValueType<ByteArray> {

    private val builder = StringBuilder()
    private var array:ByteArray? = null

    override var startPosition: Long = -1L
    override var endPosition: Long = -1L

    override val value: ByteArray
        get() = array!!

    public fun set(bytes:ByteArray):BBytes {
        array = bytes
        return this
    }

    public override fun toString():String = value.asString()

    override fun processChar(stream: BTokenStream): Boolean {
        when(stream.currentChar()) {
            ':' -> {
                val count = builder.toString().toInt()
                this.array = stream.nextBytes(count)
                endPosition = stream.position()
                return true
            }
            else -> {
                if(startPosition == -1L)
                    startPosition = stream.position()

                builder.append(stream.currentChar().toString().toInt())

                return false
            }
        }
    }

}
