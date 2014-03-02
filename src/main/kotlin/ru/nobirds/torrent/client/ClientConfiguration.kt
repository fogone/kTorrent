package ru.nobirds.torrent.client

import java.util.ArrayList
import org.springframework.context.annotation.Configuration as configuration
import org.springframework.context.annotation.Bean as bean
import org.springframework.context.annotation.ComponentScan
import ru.nobirds.torrent.config.Config
import java.util.Properties

ComponentScan(basePackages = array("ru.nobirds.torrent.client"))
public open configuration class ClientConfiguration {

    public open bean fun config():Config {
        val properties = ClassLoader
                .getSystemResourceAsStream("client.properties")!!
                .use { stream ->
            val properties = Properties()
            properties.load(stream)
            properties
        }

        return Config(properties as Map<String, String>)
    }

}