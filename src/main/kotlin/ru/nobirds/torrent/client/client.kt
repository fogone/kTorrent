package ru.nobirds.torrent.client

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import ru.nobirds.torrent.client.task.TorrentTaskManager

public fun main(args:Array<String>) {
    val applicationContext = AnnotationConfigApplicationContext()
    applicationContext.register(javaClass<ClientConfiguration>())
    applicationContext.refresh()

    val torrentTaskManager = applicationContext.getBean(javaClass<TorrentTaskManager>())!!

    torrentTaskManager.add(ClassLoader.getSystemResourceAsStream("")!!)
}