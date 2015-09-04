package ru.nobirds.torrent.utils

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import ru.nobirds.torrent.peers.Peer
import java.io.File
import java.io.RandomAccessFile
import java.math.BigInteger
import java.net.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread

fun <P:Any, R:Any> P?.nullOr(body:P.()->R):R?
        = if(this == null) null else body()

fun ByteArray.asString():String = this.toString("UTF-8")

public fun <T> Array<T>.component1():T = this[0]
public fun <T> Array<T>.component2():T = this[1]
public fun <T> Array<T>.component3():T = this[2]

public fun ByteArray.toHexString():String = BigInteger(1, this).toString(16)

public fun <K, V> multiValueMapOf(vararg pairs:Pair<K, V>):MultiValueMap<K, V> {
    val map = LinkedMultiValueMap<K, V>()

    for (pair in pairs) {
        map.put(pair.first, arrayListOf(pair.second))
    }

    return map
}

public fun ByteArray.toUrlString():String {
    return UrlUtils.encode(this)
}

public object UrlUtils {

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

        return "%${first}${second}"
    }

    public fun encode(bytes: ByteArray) : String {

        val result = StringBuffer(bytes.size() * 2)

        for (byte in bytes) {
            if(isAllowedSymbol(byte))
                result.append(byte.toChar())
            else
                result.append(encodeByte(byte))
        }

        return result.toString()
    }

}

public fun BitSet.setBits(count: Int):Sequence<Int> {
    var last = 0
    return sequence {
        val nextSetBit = nextSetBit(last)
        if (nextSetBit != -1 || nextSetBit < count) nextSetBit else null
    }
}

public fun BitSet.clearBits():Sequence<Int> {
    var last = 0
    return sequence {
        val nextClearBit = nextClearBit(last)
        if (nextClearBit != -1) nextClearBit else null
    }
}
public fun BitSet.copy():BitSet = BitSet.valueOf(this.toByteArray())

public fun <T:Any> T?.equalsNullable(other:T?):Boolean {
    if(this == null && other == null)
        return true

    if(this == null && other != null)
        return false

    if(this != null && other == null)
        return false

    return this!!.equals(other)
}

public fun ByteArray.equalsArray(other:ByteArray):Boolean = Arrays.equals(this, other)
public fun ByteArray.toId():Id = Id.fromBytes(this)

public fun List<ByteArray>.equalsList(other:List<ByteArray>):Boolean {
    if(this.size() != other.size()) return false

    for (i in 0..size() -1) {
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
public fun String.toHash(): String = MessageDigest.getInstance("MD5").digest(toByteArray())!!.toHexString()

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
    (0..size() -1).forEach {
        set(it, factory(it))
    }

    return this
}

public fun Byte.xor(byte:Byte):Byte = (toInt() xor byte.toInt()).toByte()

public fun ByteArray.parse26BytesPeers():List<Pair<Id, InetSocketAddress>> {
    val source = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)

    val id = ByteArray(20)
    val ip = ByteArray(4)

    return (0..size() / 26 - 1).map {
        source.get(id)
        source.get(ip)
        val port = source.short.toInt() and 0xffff
        val address = InetSocketAddress(InetAddress.getByAddress(ip), port)

        Pair(Id.fromBytes(id), address)
    }
}

public fun ByteArray.toInetSocketAddresses():List<InetSocketAddress> {
    val source = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)

    val ip = ByteArray(4)

    return (0..size() / 6 - 1).map {
        source.get(ip)
        val port = source.short.toInt() and 0xffff
        InetSocketAddress(InetAddress.getByAddress(ip), port)
    }
}

public fun ByteArray.toInetSocketAddress():InetSocketAddress {
    val source = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)

    val ip = ByteArray(4)

    source.get(ip)
    val port = source.short.toInt() and 0xffff

    return InetSocketAddress(InetAddress.getByAddress(ip), port)
}

public fun List<Pair<Id, InetSocketAddress>>.toCompact():ByteArray {
    val peerIdBuffer = ByteArray(20)
    val addressBuffer = ByteArray(6)

    val result = ByteArray(size() * 26)
    val buffer = ByteBuffer.wrap(result)

    for (peer in this) {
        buffer.put(peer.first.toBytes(peerIdBuffer))
        buffer.put(peer.second.toCompact(addressBuffer))
    }

    return result
}

public fun Peer.toCompact(): ByteArray {
    val compact = ByteArray(26)

    val buffer = ByteBuffer.wrap(compact)

    buffer.put(this.id.toBytes())
    buffer.put(this.address.toCompact())

    return compact
}

public fun ByteArray.copyTo(target:ByteArray, offset:Int = 0, position:Int = 0, length:Int = target.size()):ByteArray {
    System.arraycopy(this, offset, target, position, length)
    return target
}

public fun InetSocketAddress.toCompact(bytes:ByteArray = ByteArray(6)):ByteArray {
    address.address.copyTo(bytes)

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

public fun <T, R:Comparable<R>> Collection<T>.toPriorityQueue(order:(T)->R):PriorityQueue<T> {
    val queue = PriorityQueue(size(), comparator { x: T, y: T -> order(x).compareTo(order(y)) })
    queue.addAll(this)
    return queue
}

public fun <T> Queue<T>.top(count:Int):List<T> {
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

public fun Int.isPortAvailable():Boolean {
    try {
        ServerSocket(this).close()
        return true
    } catch(e: SocketException) {
        return false
    }
}

public fun byteArray(size:Int, factory:(Int)-> Byte): ByteArray {
    val data = ByteArray(size)
    for (i in 0..size - 1) {
        data[i] = factory(i)
    }
    return data
}

public fun String.hexToByteArray(): ByteArray {
    val data = ByteArray(length() / 2)

    for (i in (0..length() -1).step(2)) {
        data[i / 2] = ((Character.digit(charAt(i), 16) shl 4) + Character.digit(charAt(i + 1), 16)).toByte()
    }

    return data
}

public fun infiniteLoop(block: () -> Unit) {
    try {
        while (Thread.currentThread().isInterrupted.not()) {
            block()
        }
    } catch(e: InterruptedException) {
        // do nothing
    }
}

public fun infiniteLoopThread(block: () -> Unit):Thread = thread(start = true) { infiniteLoop(block) }

public fun <M> queueHandlerThread(queue:BlockingQueue<M>, handler: (M) -> Unit):Thread = infiniteLoopThread { handler(queue.take()) }

public fun IntRange.availablePort():Int =
        firstOrNull { it.toInt().isPortAvailable() }?.toInt()  ?: throw IllegalStateException("All configured ports used.")

