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
import java.nio.channels.SelectableChannel
import java.util.concurrent.CopyOnWriteArrayList
import java.util.Queue
import java.util.LinkedList

public data class ReadMessage(val channel: SocketChannel, val bytes: ByteArray)

public class ConnectionManager(val port:Int, val executor: ExecutorService, val threadCount:Int = 1) {

    private val selectedKeysQueue: BlockingQueue<SelectionKey> = ArrayBlockingQueue(1000)
    private val readMessageQueue: BlockingQueue<ReadMessage> = ArrayBlockingQueue(1000)
    private val writeMessages: ConcurrentHashMap<SocketAddress, Queue<ByteArray>> = ConcurrentHashMap()

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
            val channel = SocketChannel.open(address)
            channel.configureBlocking(false)

            registerInBucket(channel, bucket)
        }
    }

    public fun write(hash:Id, bytes: ByteArray) {
        val bucket = buckets.get(hash)
        if (bucket != null && bucket.notEmpty()) {
            val channel = bucket.get()
            writeMessages.getOrPut(channel.getRemoteAddress()) { LinkedList() }.offer(bytes)
            channel.register(selector, SelectionKey.OP_WRITE)
        }
    }

    public fun close(hash: Id) {
        buckets.remove(hash)?.close()
    }

    public fun read(): ReadMessage = readMessageQueue.take()

    private fun registerInBucket(channel: SocketChannel, bucket: Bucket) {
        channel.register(selector, SelectionKey.OP_READ)
        bucket.add(channel)
    }

    private fun initialize() {
        executor.execute {
            initializeServer()

            while(true)
                selectKeys()
        }
    }

    private fun initializeServer() {
        serverSocketChannel.configureBlocking(false)
        serverSocketChannel.bind(InetSocketAddress.createUnresolved("localhost", port))
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
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

                registerInBucket(socketChannel, incomingBucket)
            }

            if (selectionKey.isReadable()) {
                val channel = selectionKey.channel() as SocketChannel

                buffer.clear()

                val read = channel.read(buffer)

                val message = ByteArray(read)

                buffer.get(message)

                readMessageQueue.put(ReadMessage(channel, message))
            }

            if (selectionKey.isWritable()) {
                val channel = selectionKey.channel() as SocketChannel

                val messages = writeMessages.get(channel.getRemoteAddress())

                if(messages != null) {
                    while (messages.notEmpty) {
                        val message = messages.poll()
                        buffer.clear()
                        buffer.put(message)
                        channel.write(buffer)
                    }
                }

                // todo: unregister
            }
        }
    }

}
