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
        write('l', 'e') {
            for (item in list) {
                writeObject(item)
            }
        }
    }

    public fun write(value:BigInteger) {
        write('i', 'e') {
            stream.write(value.toString().getBytes())
        }
    }

    public fun write(map:Map<String, Any>) {
        write('d', 'e') {
            for (entry in map.entrySet()) {
                write(entry.key.toByteArray("UTF-8"))
                writeObject(entry.value)
            }
        }
    }

    public fun writeObject(value:Any) {
        when(value) {
            is Map<*, *> -> write(value as Map<String, Any>)
            is List<*> -> write(value as List<Any>)
            is BigInteger -> write(value)
            is ByteArray -> write(value)
        }
    }
}