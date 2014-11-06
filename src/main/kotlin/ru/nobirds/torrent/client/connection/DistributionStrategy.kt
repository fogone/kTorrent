package ru.nobirds.torrent.client.connection

import java.nio.channels.SocketChannel
import java.util.Random

trait DistributionStrategy {

    fun add(channel: SocketChannel)

    fun remove(channel: SocketChannel)

    fun distribute(): SocketChannel

}
