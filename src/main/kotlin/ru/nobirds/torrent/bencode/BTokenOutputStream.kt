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

    public fun write(list:List<Any>) {
        writeList(list) { writeObject(it) }
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

    public fun write(map:Map<String, Any>) {
        writeMap(map.entrySet()) {
            write(it.key.toByteArray("UTF-8"))
            writeObject(it.value)
        }
    }

    public fun write(map:BMap) {
        writeMap(map.pairs) { write(it) }
    }

    public fun write(pair:BKeyValuePair) {
        write(pair.name.toByteArray("UTF-8"))
        writeBObject(pair.bvalue)
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

    public fun writeObject(value:Any) {
        when(value) {
            is Map<*, *> -> write(value as Map<String, Any>)
            is List<*> -> write(value as List<Any>)
            is BigInteger -> write(value)
            is ByteArray -> write(value)
        }
    }

    public fun writeBObject(value:BType<out Any>) {
        when(value) {
            is BMap -> write(value)
            is BKeyValuePair -> write(value)
            is BList -> write(value)
            is BNumber -> write(value)
            is BBytes -> write(value)
        }
    }
}