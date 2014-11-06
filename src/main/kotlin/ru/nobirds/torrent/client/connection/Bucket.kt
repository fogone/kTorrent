package ru.nobirds.torrent.client.connection

import java.net.SocketAddress
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.Random
import java.util.ArrayList

class Bucket() {

    private val distributionStrategy: DistributionStrategy = object : DistributionStrategy {

        private val random = Random()
        private val channels = ArrayList<SocketChannel>()

        override fun add(channel: SocketChannel) {
            channels.add(channel)
        }

        override fun remove(channel: SocketChannel) {
            channels.remove(channel)
        }

        override fun distribute(): SocketChannel {
            val index = random.nextInt(channels.size - 1)
            return channels[index]
        }

    }

    private val channels = ConcurrentHashMap<SocketAddress, SocketChannel>()

    fun get(): SocketChannel = distributionStrategy.distribute()

    fun add(channel: SocketChannel) {
        val address = channel.getRemoteAddress()

        channels.remove(address)?.close()

        channels.put(address, channel)
        distributionStrategy.add(channel)
    }

    fun remove(channel: SocketChannel) {
        val address = channel.getLocalAddress()
        channels.remove(address)?.close()
        distributionStrategy.remove(channel)
    }

    fun contains(address: SocketAddress): Boolean {
        val socketChannel = channels[address]
        return socketChannel != null && socketChannel.isOpen()
    }

    fun close() {
        for (channel in channels.values()) {
            channel.close()
        }
    }

    fun notEmpty(): Boolean = !channels.empty

}