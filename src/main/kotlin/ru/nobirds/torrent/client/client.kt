package ru.nobirds.torrent.client

import ru.nobirds.torrent.client.task.TorrentTaskManager
import ru.nobirds.torrent.config.Configs

public class Client() {

    public val taskManager:TorrentTaskManager = TorrentTaskManager(Configs.fromProperties("client.properties"))

    public fun start() {
        try {
            process()
        } finally {
            // todo
        }
    }

    protected fun process() {
        while(!Thread.currentThread().isInterrupted()) {
            // todo
        }
    }
}

public fun main(args:Array<String>) {
    val client = Client()

    client.taskManager.add(ClassLoader.getSystemResourceAsStream("torrent2.torrent")!!)
    client.start()
}