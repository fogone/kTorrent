package ru.nobirds.torrent.client

import org.junit.Test
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.ContextConfiguration
import org.springframework.beans.factory.annotation.Autowired
import ru.nobirds.torrent.client.task.TorrentTaskManager

RunWith(javaClass<SpringJUnit4ClassRunner>())
ContextConfiguration(classes = array(javaClass<ClientConfiguration>()))
public class ClientTest {

    private Autowired var taskManager:TorrentTaskManager? = null

    Test
    public fun test1() {

        taskManager!!.add(ClassLoader.getSystemResourceAsStream("test1.torrent")!!)

    }

}