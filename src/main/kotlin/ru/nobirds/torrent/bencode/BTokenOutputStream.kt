package ru.nobirds.torrent.bencode

import java.io.OutputStream
import java.math.BigInteger

public class BTokenOutputStream(val stream:OutputStream) {

    public fun write(start:Char, end:Char, body:()->Unit) {
        stream.write(start.toInt())
        body()
        stream.write(end.toInt())
    }

    public fun write(bytes:ByteArray) {
        stream.write(bytes.size.toString().getBytes())
        stream.write(':'.toInt())
        stream.write(bytes)
    }

    public fun write(value:BigInteger) {
        write('i', 'e') {
            stream.write(value.toString().getBytes())
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
}