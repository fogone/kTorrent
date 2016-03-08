package ru.nobirds.torrent.client.task

class TrafficStatistics() {

    private var total:Long = 0

    val totalInBytes:Long
        get() = total

    fun process(count:Long) {
        total += count
    }

}