package ru.nobirds.torrent.client.connection

import java.nio.channels.SocketChannel

interface DistributionStrategy {

    fun add(channel: SocketChannel)

    fun remove(channel: SocketChannel)

    fun distribute(): SocketChannel

}
