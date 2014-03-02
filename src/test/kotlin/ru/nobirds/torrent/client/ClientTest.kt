package ru.nobirds.torrent.client

import org.junit.Test
import org.springframework.context.annotation.AnnotationConfigApplicationContext

public class ClientTest {


    Test
    public fun test1() {
        val applicationContext = AnnotationConfigApplicationContext()
        applicationContext.register(javaClass<TorrentClientConfiguration>())
        applicationContext.refresh()

        applicationContext.close()
    }

}