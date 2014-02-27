package ru.nobirds.torrent.bencode

public class BBytes() : AbstractBType<ByteArray>() {

    private val builder = StringBuilder()
    private var array:ByteArray? = null

    override val value: ByteArray
        get() = array!!

    public fun toString():String = String(value, "UTF-8")

    override fun processChar(stream: BTokenInputStream): Boolean {
        when(stream.currentChar()) {
            ':' -> {
                val count = builder.toString().toInt()
                this.array = stream.nextBytes(count)
                return true
            }
            else -> {
                builder.append(stream.currentChar().toString().toInt())
                return false
            }
        }
    }

}
