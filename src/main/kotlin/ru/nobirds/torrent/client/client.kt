package ru.nobirds.torrent.client

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import ru.nobirds.torrent.client.task.TorrentTaskManager
import org.springframework.context.ApplicationContext
import ru.nobirds.torrent.configureApplicationContext

public class Client() {

    private val applicationContext = javaClass<ClientConfiguration>()
            .configureApplicationContext()

    public val taskManager:TorrentTaskManager = applicationContext.getBean(javaClass<TorrentTaskManager>())!!

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

    client.taskManager.add(ClassLoader.getSystemResourceAsStream("torrent1.torrent")!!)
    client.start()
}