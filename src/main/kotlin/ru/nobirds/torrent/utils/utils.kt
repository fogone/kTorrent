package ru.nobirds.torrent.utils

import io.netty.buffer.ByteBuf
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.io.File
import java.io.RandomAccessFile
import java.math.BigInteger
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.ArrayList
import java.util.Arrays
import java.util.BitSet
import java.util.Collections
import java.util.PriorityQueue
import java.util.Queue
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.BlockingQueue
import kotlin.comparisons.compareBy
import kotlin.concurrent.thread

fun ByteArray.asString():String = String(this)

fun <T> Array<T>.component1():T = this[0]
fun <T> Array<T>.component2():T = this[1]
fun <T> Array<T>.component3():T = this[2]

fun ByteArray.toHexString():String = BigInteger(1, this).toString(16)

fun <K, V> multiValueMapOf(vararg pairs:Pair<K, V>):MultiValueMap<K, V> {
    val map = LinkedMultiValueMap<K, V>()

    for (pair in pairs) {
        map.put(pair.first, arrayListOf(pair.second))
    }

    return map
}

fun ByteArray.toUrlString():String {
    return UrlUtils.encode(this)
}

object UrlUtils {

    private val allowedSymbols =
            ('a'..'z').map { it.toByte() } +
            ('A'..'Z').map { it.toByte() } +
            ('0'..'9').map { it.toByte() } +
            arrayOf('-', '_', '.', '!').map { it.toByte() }

    private val hex = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    private fun isAllowedSymbol(byte:Byte):Boolean = byte in allowedSymbols

    private fun encodeByte(byte:Byte):String {
        var ch = (byte.toInt() and 240).toByte()
        ch = (ch.toInt().ushr(4)).toByte()
        ch = (ch.toInt() and 15).toByte()

        val first = hex[ch.toInt()]

        ch = (byte.toInt() and 15).toByte()

        val second = hex[ch.toInt()]

        return "%$first$second"
    }

    fun encode(bytes: ByteArray) : String {

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

fun BitSet.setBits(count: Int):Sequence<Int> {
    var last = -1
    return generateSequence {
        last = nextSetBit(last+1)
        if (last != -1 && last < count) last else null
    }
}

fun BitSet.clearBits():Sequence<Int> {
    var last = -1
    return generateSequence {
        last = nextClearBit(last+1)
        if (last != -1) last else null
    }
}

fun BitSet.copy():BitSet = BitSet.valueOf(this.toByteArray())

fun <T:Any> T?.equalsNullable(other:T?):Boolean {
    if(this == null && other == null)
        return true

    if(this == null && other != null)
        return false

    if(this != null && other == null)
        return false

    return this!!.equals(other)
}

fun ByteArray.equalsArray(other:ByteArray):Boolean = Arrays.equals(this, other)
fun ByteArray.toId():Id = Id.fromBytes(this)

fun List<ByteArray>.equalsList(other:List<ByteArray>):Boolean {
    if(this.size != other.size) return false

    for (i in 0..size -1) {
        if(!get(i).equalsArray(other.get(i)))
            return false
    }

    return true
}

fun RandomAccessFile.closeQuietly() {
    try {
        close()
    } catch(e:Exception){
        // no reaction
    }
}

fun File.randomAccess(mode:String = "rw"):RandomAccessFile = RandomAccessFile(this, mode)

fun String.containsNonPrintable():Boolean = any { it.toInt() !in 32..127 }
fun String.toHash(): String = MessageDigest.getInstance("MD5").digest(toByteArray())!!.toHexString()

fun Int.divToUp(value:Int):Int = (this + value - 1) / value
fun Long.divToUp(value:Long):Long = (this + value - 1L) / value

data class IterationStatus<T>(private val iterator:Iterator<T>) {

    private var index:Int = 0
    private var value:T = iterator.next()
    private var hasNext:Boolean = iterator.hasNext()

    fun next():IterationStatus<T> {
        this.index++
        this.value = iterator.next()
        this.hasNext = iterator.hasNext()
        return this
    }

    fun value():T = value
    fun hasNext():Boolean = hasNext
    fun index():Int = index

}

fun <T> Iterable<T>.forEachWithStatus(block:(IterationStatus<T>)->Unit) {
    iterator().forEachWithStatus(block)
}

fun <T> Iterator<T>.forEachWithStatus(block:(IterationStatus<T>)->Unit) {
    if(!this.hasNext())
        return

    val status = IterationStatus(this)
    block(status)
    while(status.hasNext()) {
        block(status.next())
    }

}

inline fun ByteArray.fillWith(factory:(Int)->Byte):ByteArray {
    indices.forEach {
        set(it, factory(it))
    }

    return this
}

infix fun Byte.xor(byte:Byte):Byte = (toInt() xor byte.toInt()).toByte()

fun ByteArray.parse26BytesPeers():List<Pair<Id, InetSocketAddress>> {
    val source = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)

    val id = ByteArray(20)
    val ip = ByteArray(4)

    return (0..size / 26 - 1).map {
        source.get(id)
        source.get(ip)
        val port = source.short.toInt() and 0xffff
        val address = InetSocketAddress(InetAddress.getByAddress(ip), port)

        Pair(Id.fromBytes(id), address)
    }
}

fun ByteArray.toInetSocketAddresses():List<InetSocketAddress> {
    val source = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)

    val ip = ByteArray(4)

    return (0..size / 6 - 1).map {
        source.get(ip)
        val port = source.short.toInt() and 0xffff
        InetSocketAddress(InetAddress.getByAddress(ip), port)
    }
}

fun ByteArray.toInetSocketAddress():InetSocketAddress {
    val source = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)

    val ip = ByteArray(4)

    source.get(ip)
    val port = source.short.toInt() and 0xffff

    return InetSocketAddress(InetAddress.getByAddress(ip), port)
}

fun List<Pair<Id, InetSocketAddress>>.toCompact():ByteArray {
    val peerIdBuffer = ByteArray(20)
    val addressBuffer = ByteArray(6)

    val result = ByteArray(size * 26)
    val buffer = ByteBuffer.wrap(result)

    for (peer in this) {
        buffer.put(peer.first.toBytes(peerIdBuffer))
        buffer.put(peer.second.toCompact(addressBuffer))
    }

    return result
}

/*
public fun Peer.toCompact(): ByteArray {
    val compact = ByteArray(26)

    val buffer = ByteBuffer.wrap(compact)

    buffer.put(this.id.toBytes())
    buffer.put(this.address.toCompact())

    return compact
}
*/

fun ByteArray.copyTo(target:ByteArray, offset:Int = 0, position:Int = 0, length:Int = target.size):ByteArray {
    System.arraycopy(this, offset, target, position, length)
    return target
}

fun InetSocketAddress.toCompact(bytes:ByteArray = ByteArray(6)):ByteArray {
    address.address.copyTo(bytes)

    bytes[4] = (port or 0xff).toByte()
    bytes[5] = (port shl 2 or 0xff).toByte()

    return bytes
}

fun Timer.scheduleOnce(timeout:Long, callback:()->Unit):TimerTask {
    val task = object : TimerTask() {
        override fun run() { callback()}
    }

    schedule(task, timeout)

    return task
}

fun <T, R:Comparable<R>> Collection<T>.toPriorityQueue(order:(T)->R):PriorityQueue<T> {
    val queue = PriorityQueue<T>(size, compareBy { order(it) })
    queue.addAll(this)
    return queue
}

fun <T> Queue<T>.top(count:Int):List<T> {
    if(isEmpty())
        return Collections.emptyList()

    val result = ArrayList<T>()

    var copy = 1
    var item = poll()

    while(item != null) {
        result.add(item)
        item = if (copy++ < count) poll() else null
    }

    return result
}

fun Int.isPortAvailable():Boolean {
    try {
        ServerSocket(this).close()
        return true
    } catch(e: SocketException) {
        return false
    }
}

fun byteArray(size:Int, factory:(Int)-> Byte): ByteArray {
    val data = ByteArray(size)
    for (i in 0..size - 1) {
        data[i] = factory(i)
    }
    return data
}

fun String.hexToByteArray(): ByteArray {
    val data = ByteArray(length / 2)

    for (i in (0..length -1).step(2)) {
        data[i / 2] = ((Character.digit(get(i), 16) shl 4) + Character.digit(get(i + 1), 16)).toByte()
    }

    return data
}

fun infiniteLoop(block: () -> Unit) {
    while (Thread.currentThread().isInterrupted.not()) {
        try {
            block()
        } catch(e: InterruptedException) {
            // do nothing
        } catch(e: Exception) {
            e.printStackTrace()
            break
        }
    }
}

fun infiniteLoopThread(block: () -> Unit):Thread =
        thread(start = true) { infiniteLoop(block) }

fun <M> queueHandlerThread(queue:BlockingQueue<M>, handler: (M) -> Unit):Thread =
        infiniteLoopThread { handler(queue.take()) }

fun IntRange.availablePort():Int =
        firstOrNull { it.toInt().isPortAvailable() }?.toInt()  ?: throw IllegalStateException("All configured ports used.")


fun ByteBuf.rewind(bytesCount:Int): ByteBuf {
    readerIndex(readerIndex() + bytesCount)
    return this
}

inline fun <reified T:Any> T.log():Logger = LoggerFactory.getLogger(T::class.java)