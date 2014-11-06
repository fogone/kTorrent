package ru.nobirds.torrent.client.connection

import java.util.concurrent.ExecutorService
import java.util.concurrent.Callable
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ArrayBlockingQueue
import java.nio.channels.SelectableChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider
import java.nio.channels.SelectionKey
import java.nio.ByteBuffer
import java.util.HashMap
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.peers.Peer
import java.nio.channels.ServerSocketChannel
import java.net.InetSocketAddress

public open class ConnectionMessage(val channel: SocketChannel)
public class ReadMessage(channel: SocketChannel, val bytes: ByteArray) : ConnectionMessage(channel)
public class WriteMessage(channel: SocketChannel, val bytes: ByteArray) : ConnectionMessage(channel)

public class ConnectionContainer(val port:Int, val executor: ExecutorService, val threadCount:Int = 1) {

    private val selectedKeysQueue: BlockingQueue<SelectionKey> = ArrayBlockingQueue(1000)
    private val messageQueue: BlockingQueue<ByteArray> = ArrayBlockingQueue(1000)

    private val serverSocketChannel = ServerSocketChannel.open()

    private val channels = HashMap<Id, SelectableChannel>()

    private val workers = (0..threadCount).map { TransferDataWorker(selectedKeysQueue, messageQueue) }

    private val futures = executor.invokeAll(workers)

    private val selector = SelectorProvider.provider().openSelector()

    ;{ start() }

    public fun add(peer: Peer) {
        val channel = SocketChannel
                .open(peer.address)
                .configureBlocking(false)

        channels.put(peer.id, channel)

        channel.register(selector, SelectionKey.OP_READ)
    }

    public fun write(peer:Id, bytes: ByteArray) {

    }

    fun start() {
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

    class TransferDataWorker(val selectedReadKeysQueue: BlockingQueue<SelectionKey>,
                             val messagesQueue: BlockingQueue<ByteArray>) : Callable<Unit> {

        private val buffer = ByteBuffer.allocate(50 * 1024)

        override fun call() {
            while (true) {
                val selectionKey = selectedReadKeysQueue.take()

                if (selectionKey.isAcceptable()) {
                    val serverSocketChannel = selectionKey.channel() as ServerSocketChannel
                    val socketChannel = serverSocketChannel.accept()
                }

                val channel = selectionKey.channel() as SocketChannel

                buffer.clear()

                val read = channel.read(buffer)

                val message = ByteArray(read)

                buffer.get(message)

                messagesQueue.put(message)
            }
        }

    }

}
