package ru.nobirds.torrent.bencode

import io.netty.buffer.ByteBuf
import java.io.OutputStream
import java.math.BigInteger

interface BTokenWriter {

    fun write(start:Char, end:Char, body:()->Unit) {
        writeImpl(start.toInt())
        body()
        writeImpl(end.toInt())
    }

    fun write(bytes:ByteArray) {
        writeImpl(bytes.size.toString().toByteArray())
        writeImpl(':'.toInt())
        writeImpl(bytes)
    }

    fun write(value:BigInteger) {
        write('i', 'e') {
            writeImpl(value.toString().toByteArray())
        }
    }

    fun <T> writeList(iterable:Iterable<T>, writer:(T)->Unit) {
        write('l', 'e') {
            for (item in iterable) {
                writer(item)
            }
        }
    }

    fun <P> writeMap(pairs:Iterable<P>, writer:(P)->Unit) {
        write('d', 'e') {
            for (pair in pairs) {
                writer(pair)
            }
        }
    }
    fun write(map:BMap) {
        writeMap(map.values) { write(it) }
    }

    fun write(pair:BKeyValuePair) {
        write(pair.name.toByteArray())
        writeBObject(pair.value)
    }

    fun write(list:BList) {
        writeList(list) { writeBObject(it) }
    }

    fun write(number:BNumber) {
        write(number.value)
    }

    fun write(bytes:BBytes) {
        write(bytes.value)
    }

    fun writeBObject(value:BType) {
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

class BTokenOutputStream(val stream:OutputStream) : BTokenWriter {

    override fun writeImpl(byte: Int) {
        stream.write(byte)
    }

    override fun writeImpl(bytes: ByteArray) {
        stream.write(bytes)
    }

}

class BTokenBufferWriter(val buffer:ByteBuf) : BTokenWriter {

    override fun writeImpl(byte: Int) {
        buffer.writeByte(byte)
    }

    override fun writeImpl(bytes: ByteArray) {
        buffer.writeBytes(bytes)
    }

}