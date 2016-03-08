package ru.nobirds.torrent.client

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import ru.nobirds.torrent.client.task.TaskManager
import java.nio.file.Paths

fun main(args:Array<String>) {
    SpringApplication.run(arrayOf(TorrentClientConfiguration::class.java), args)
}

open class ClientCommandLineRunner(val taskManager: TaskManager) : CommandLineRunner {

    override fun run(vararg args: String?) {
        taskManager.add(ClassLoader.getSystemResourceAsStream("torrent1.torrent"), Paths.get("d:\\tmp\\torrents\\"))
    }

}