package ru.nobirds.torrent

import java.math.BigInteger
import java.util.HashMap
import org.springframework.util.MultiValueMap
import org.springframework.util.LinkedMultiValueMap
import java.net.Socket
import java.util.BitSet

fun <P, R> P?.nullOr(body:P.()->R):R?
        = if(this == null) null else body()

fun ByteArray.asString():String = this.toString("UTF-8")

public fun <T> Array<T>.component1():T = this[0]
public fun <T> Array<T>.component2():T = this[1]
public fun <T> Array<T>.component3():T = this[2]

public fun ByteArray.toHexString():String = BigInteger(1, this).toString(16)

public fun <T, R> Iterable<T>.toMap(mapper:(T)->R):Map<R, T> {
    val result = HashMap<R, T>()

    for (item in this) {
        result[mapper(item)] = item
    }

    return result
}

public fun <K, V> multiValueMapOf(vararg pairs:Pair<K, V>):MultiValueMap<K, V> {
    val map = LinkedMultiValueMap<K, V>()

    for (pair in pairs) {
        map.put(pair.first, arrayListOf(pair.second))
    }

    return map
}

public fun Socket.closeQuietly() {
    try {
        close()
    } catch(e:Exception) {
    }
}

public fun ByteArray.toUrlString():String {
    return UrlUtils.encode(this)
}

public object UrlUtils {

    private val allowedSymbols =
            ('a'..'z').map { it.toByte() } +
            ('A'..'Z').map { it.toByte() } +
            ('0'..'9').map { it.toByte() } +
            array('-', '_', '.', '!').map { it.toByte() }

    private val hex = array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    private fun isAllowedSymbol(byte:Byte):Boolean = byte in allowedSymbols

    private fun encodeByte(byte:Byte):String {
        var ch = (byte.toInt() and 240).toByte()
        ch = (ch.toInt().ushr(4)).toByte()
        ch = (ch.toInt() and 15).toByte()

        val first = hex[ch.toInt()]

        ch = (byte.toInt() and 15).toByte()

        val second = hex[ch.toInt()]

        return "%${first}${second}"
    }

    public fun encode(bytes: ByteArray) : String {

        val result = StringBuffer(bytes.size * 2)

        for (byte in bytes) {
            if(isAllowedSymbol(byte))
                result.append(byte.toChar())
            else
                result.append(encodeByte(byte))
        }

        return result.toString()
    }

}

public fun BitSet.isAllSet():Boolean = cardinality() == size()

public fun BitSet.setAll(value:Boolean = true) { set(0, size(), value) }

public fun BitSet.each(value:Boolean, block:(Boolean, Int)->Boolean) {
    var index = if(value) nextSetBit(0) else nextClearBit(0)
    while(index != -1) {
        set(index, block(value, index))
        index = if(value) nextSetBit(index) else nextClearBit(index)
    }
}

public fun BitSet.eachSet(block:(Boolean, Int)->Boolean) {
    each(true, block)
}

public fun BitSet.eachClear(block:(Boolean, Int)->Boolean) {
    each(false, block)
}