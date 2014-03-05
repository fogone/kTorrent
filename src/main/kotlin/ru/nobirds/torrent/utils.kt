package ru.nobirds.torrent

import java.math.BigInteger
import java.util.HashMap
import org.springframework.util.MultiValueMap
import org.springframework.util.LinkedMultiValueMap
import java.net.Socket

fun <P, R> P?.nullOr(body:P.()->R):R?
        = if(this == null) null else body()

fun ByteArray.asString():String = String(this, "UTF-8")

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

