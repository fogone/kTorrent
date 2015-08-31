package ru.nobirds.torrent.bencode

import io.netty.buffer.ByteBuf
import java.io.OutputStream
import java.math.BigInteger

public interface BTokenWriter {

    public fun write(start:Char, end:Char, body:()->Unit) {
        writeImpl(start.toInt())
        body()
        writeImpl(end.toInt())
    }

    public fun write(bytes:ByteArray) {
        writeImpl(bytes.size().toString().toByteArray())
        writeImpl(':'.toInt())
        writeImpl(bytes)
    }

    public fun write(value:BigInteger) {
        write('i', 'e') {
            writeImpl(value.toString().toByteArray())
        }
    }

    public fun writeList<T>(iterable:Iterable<T>, writer:(T)->Unit) {
        write('l', 'e') {
            for (item in iterable) {
                writer(item)
            }
        }
    }

    public fun writeMap<P>(pairs:Iterable<P>, writer:(P)->Unit) {
        write('d', 'e') {
            for (pair in pairs) {
                writer(pair)
            }
        }
    }
    public fun write(map:BMap) {
        writeMap(map.values()) { write(it) }
    }

    public fun write(pair:BKeyValuePair) {
        write(pair.name.toByteArray("UTF-8"))
        writeBObject(pair.value)
    }

    public fun write(list:BList) {
        writeList(list) { writeBObject(it) }
    }

    public fun write(number:BNumber) {
        write(number.value)
    }

    public fun write(bytes:BBytes) {
        write(bytes.value)
    }

    public fun writeBObject(value:BType) {
        when(value) {
            is BMap -> write(value)
            is BKeyValuePair -> write(value)
            is BList -> write(value)
            is BNumber -> write(value)
            is BBytes -> write(value)
        }
    }

    fun writeImpl(byte:Int)
    fun writeImpl(bytes:ByteArray)

}

public class BTokenOutputStream(val stream:OutputStream) : BTokenWriter {

    override fun writeImpl(byte: Int) {
        stream.write(byte)
    }

    override fun writeImpl(bytes: ByteArray) {
        stream.write(bytes)
    }

}

public class BTokenBufferWriter(val buffer:ByteBuf) : BTokenWriter {

    override fun writeImpl(byte: Int) {
        buffer.writeByte(byte)
    }

    override fun writeImpl(bytes: ByteArray) {
        buffer.writeBytes(bytes)
    }

}