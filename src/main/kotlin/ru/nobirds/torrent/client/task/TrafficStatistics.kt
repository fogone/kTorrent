package ru.nobirds.torrent.client.task

public class TrafficStatistics() {

    private var total:Long = 0

    public val totalInBytes:Long
        get() = total

    public fun process(count:Long) {
        total += count
    }

}