package ru.nobirds.torrent.utils

import java.math.BigInteger
import java.util.HashMap
import org.springframework.util.MultiValueMap
import org.springframework.util.LinkedMultiValueMap
import java.net.Socket
import java.util.BitSet
import java.util.Arrays
import java.io.RandomAccessFile
import java.io.File
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.net.InetSocketAddress
import java.util.Timer
import java.util.TimerTask
import java.util.PriorityQueue
import java.util.Queue
import java.util.ArrayList
import java.util.Collections
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorContext
import akka.actor.ActorRefFactory

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

public fun BitSet.isAllSet(size:Int):Boolean = cardinality() == size

public fun BitSet.setAll(size:Int, value:Boolean = true) { set(0, size, value) }

public fun BitSet.each(size:Int, value:Boolean, block:(Boolean, Int)->Boolean) {
    var index = if(value) nextSetBit(0) else nextClearBit(0)
    while(index != -1 && index <= size) {
        set(index, block(value, index))
        index = if(value) nextSetBit(index+1) else nextClearBit(index+1)
    }
}

public fun BitSet.findIndex(size:Int, value:Boolean, predicate:(Int)->Boolean):Int {
    var index = if(value) nextSetBit(0) else nextClearBit(0)
    while(index != -1 && index <= size) {
        if(predicate(index))
            return index
        index = if(value) nextSetBit(index+1) else nextClearBit(index+1)
    }

    return -1
}

public fun BitSet.eachSet(size:Int, block:(Boolean, Int)->Boolean) {
    each(size, true, block)
}

public fun BitSet.eachClear(size:Int, block:(Boolean, Int)->Boolean) {
    each(size, false, block)
}

public fun BitSet.copy():BitSet = this.clone() as BitSet

public fun <T> T?.equalsNullable(other:T?):Boolean {
    if(this == null && other == null)
        return true

    if(this == null && other != null)
        return false

    if(this != null && other == null)
        return false

    return this.equals(other)
}

public fun ByteArray.equalsArray(other:ByteArray):Boolean = Arrays.equals(this, other)

public fun List<ByteArray>.equalsList(other:List<ByteArray>):Boolean {
    if(this.size != other.size) return false

    for (i in 0..size-1) {
        if(!get(i).equalsArray(other.get(i)))
            return false
    }

    return true
}

public fun RandomAccessFile.closeQuietly() {
    try {
        close()
    } catch(e:Exception){
        // no reaction
    }
}

public fun File.randomAccess(mode:String = "rw"):RandomAccessFile = RandomAccessFile(this, mode)

public fun String.containsNonPrintable():Boolean = any { it.toInt() !in 32..127 }

public fun Int.divToUp(value:Int):Int = (this + value - 1) / value
public fun Long.divToUp(value:Long):Long = (this + value - 1L) / value

public data class IterationStatus<T>(private val iterator:Iterator<T>) {

    private var index:Int = 0
    private var value:T = iterator.next()
    private var hasNext:Boolean = iterator.hasNext()

    public fun next():IterationStatus<T> {
        this.index++
        this.value = iterator.next()
        this.hasNext = iterator.hasNext()
        return this
    }

    public fun value():T = value
    public fun hasNext():Boolean = hasNext
    public fun index():Int = index

}

public fun <T> Iterable<T>.forEachWithStatus(block:(IterationStatus<T>)->Unit) {
    iterator().forEachWithStatus(block)
}

public fun <T> Iterator<T>.forEachWithStatus(block:(IterationStatus<T>)->Unit) {
    if(!this.hasNext())
        return

    val status = IterationStatus(this)
    block(status)
    while(status.hasNext()) {
        block(status.next())
    }

}

public inline fun ByteArray.fillWith(factory:(Int)->Byte):ByteArray {
    (0..size-1).forEach {
        set(it, factory(it))
    }

    return this
}

public fun Byte.xor(byte:Byte):Byte = (toInt() xor byte.toInt()).toByte()

public fun ByteArray.toInetSocketAddresses():List<InetSocketAddress> {
    val source = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)

    val ip = ByteArray(4)

    return (0..size / 6 - 1).map {
        source.get(ip)
        val port = source.getShort().toInt() and 0xffff
        InetSocketAddress(InetAddress.getByAddress(ip), port)
    }
}

public fun ByteArray.toInetSocketAddress():InetSocketAddress {
    val source = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)

    val ip = ByteArray(4)

    source.get(ip)
    val port = source.getShort().toInt() and 0xffff

    return InetSocketAddress(InetAddress.getByAddress(ip), port)
}

public fun <A:InetSocketAddress> List<A>.toCompact():ByteArray {
    val addressBuffer = ByteArray(size * 6)
    val result = ByteArray(size * 6)
    val buffer = ByteBuffer.wrap(result)

    for (address in this)
        buffer.put(address.toCompact(addressBuffer))

    return result
}

public fun ByteArray.copyTo(target:ByteArray, offset:Int = 0, position:Int = 0, length:Int = size) {
    System.arraycopy(this, offset, target, position, length)
}

public fun InetSocketAddress.toCompact(bytes:ByteArray = ByteArray(6)):ByteArray {
    getAddress()!!.getAddress()!!.copyTo(bytes)

    val port = getPort()
    bytes[4] = (port or 0xff).toByte()
    bytes[5] = (port shl 2 or 0xff).toByte()

    return bytes
}

public fun Timer.scheduleOnce(timeout:Long, callback:()->Unit):TimerTask {
    val task = object : TimerTask() {
        override fun run() { callback()}
    }

    schedule(task, timeout)

    return task
}

public fun <T, R:Comparable<R>> List<T>.toPriorityQueue(order:(T)->R):PriorityQueue<T> {
    val queue = PriorityQueue<T>(size, comparator<T> {(x: T, y: T) -> order(x).compareTo(order(y)) })
    queue.addAll(this)
    return queue
}

public fun <T> Queue<T>.top(count:Int):List<T> {
    if(empty)
        return Collections.emptyList()

    val result = ArrayList<T>()

    var copy = 1
    var item = poll()

    while(item != null) {
        result.add(item!!)
        item = if (copy++ < count) poll() else null
    }

    return result
}

public fun <T:Actor> ActorRefFactory.actorOf(name:String, factory:()->T):ActorRef = this.actorOf(Props.create(factory), name)!!