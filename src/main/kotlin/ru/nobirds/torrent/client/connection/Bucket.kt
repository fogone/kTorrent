package ru.nobirds.torrent.client.connection

import java.net.SocketAddress
import java.nio.channels.SelectionKey
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
            if(channels.size() == 1) return channels.first()

            val index = random.nextInt(channels.size() - 1)

            return channels[index]
        }

    }

    private val keys = ConcurrentHashMap<SocketAddress, SelectionKey>()

    fun get(): SocketChannel = distributionStrategy.distribute()

    fun add(key:SelectionKey) {
        val channel = key.channel() as SocketChannel
        val address = channel.remoteAddress

        keys.remove(address)?.channel()?.close()

        keys.put(address, key)
        distributionStrategy.add(channel)
    }

    fun remove(channel: SocketChannel) {
        val address = channel.localAddress
        keys.remove(address)?.channel()?.close()
        distributionStrategy.remove(channel)
    }

    fun contains(address: SocketAddress): Boolean {
        val key = keys[address]
        return key != null && key.isValid
    }

    fun close() {
        for (key in keys.values()) {
            key.channel()?.close()
        }
    }

    fun notEmpty(): Boolean = keys.isNotEmpty()

}