package ru.nobirds.torrent.client

import org.junit.Test
import ru.nobirds.torrent.client.task.TorrentTaskManager
import ru.nobirds.torrent.config.Config
import java.util.HashMap
import java.util.Properties
import ru.nobirds.torrent.config.Configs

public class ClientTest {

    Test
    public fun test1() {
        val config = Configs.fromProperties("client.properties")

        val taskManager = TorrentTaskManager(config)

        taskManager.add(ClassLoader.getSystemResourceAsStream("test1.torrent")!!)

    }

}