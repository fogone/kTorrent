package ru.nobirds.torrent.client.connection

import java.util.concurrent.ExecutorService
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ArrayBlockingQueue
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider
import java.nio.channels.SelectionKey
import java.util.HashMap
import ru.nobirds.torrent.utils.Id
import java.nio.channels.ServerSocketChannel
import java.net.InetSocketAddress
import java.util.concurrent.Callable
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.net.SocketAddress

public open class ConnectionMessage(val channel: SocketChannel)
public class ReadMessage(channel: SocketChannel, val bytes: ByteArray) : ConnectionMessage(channel)
public class WriteMessage(channel: SocketChannel, val bytes: ByteArray) : ConnectionMessage(channel)


class Bucket() {

    val channels = ConcurrentHashMap<SocketAddress, SocketChannel>()

    fun add(channel: SocketChannel) {
        val address = channel.getRemoteAddress()

        channels.remove(address)?.close()

        channels.put(address, channel)
    }

    fun contains(address: SocketAddress): Boolean {
        val socketChannel = channels[address]
        return socketChannel != null && socketChannel.isOpen()
    }

}

public class ConnectionManager(val port:Int, val executor: ExecutorService, val threadCount:Int = 1) {

    private val selectedKeysQueue: BlockingQueue<SelectionKey> = ArrayBlockingQueue(1000)
    private val messageQueue: BlockingQueue<ByteArray> = ArrayBlockingQueue(1000)

    private val serverSocketChannel = ServerSocketChannel.open()

    private val buckets = HashMap<Id, Bucket>()
    private val incomingBucket = Bucket()

    private val workers = (0..threadCount).map { createWorker() }

    private val futures = executor.invokeAll(workers)

    private val selector = SelectorProvider.provider().openSelector()

    ;{ initialize() }

    public fun add(hash:Id, address: SocketAddress) {
        val bucket = buckets.getOrPut(hash) { Bucket() }

        if(address !in bucket) {
            val channel = SocketChannel
                    .open(address)
                    .configureBlocking(false)

            bucket.add(channel as SocketChannel)

            channel.register(selector, SelectionKey.OP_READ)
        }
    }

    fun initialize() {
        executor.execute {
            serverSocketChannel.configureBlocking(false)
            serverSocketChannel.bind(InetSocketAddress.createUnresolved("localhost", port))
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)

            while(true)
                selectKeys()
        }
    }

    private fun selectKeys() {
        selector.select()
        val selectedKeys = selector.selectedKeys()
        for (key in selectedKeys) {
            if (key.isValid())
                selectedKeysQueue.put(key)
        }
        selectedKeys.clear()
    }

    private fun createWorker(): Callable<Unit> = Callable {

        val buffer = ByteBuffer.allocate(50 * 1024)

        while (true) {
            val selectionKey = selectedKeysQueue.take()

            if (selectionKey.isAcceptable()) {
                val serverSocketChannel = selectionKey.channel() as ServerSocketChannel
                val socketChannel = serverSocketChannel.accept()
            }

            val channel = selectionKey.channel() as SocketChannel

            buffer.clear()

            val read = channel.read(buffer)

            val message = ByteArray(read)

            buffer.get(message)

            messageQueue.put(message)
        }
    }

}
