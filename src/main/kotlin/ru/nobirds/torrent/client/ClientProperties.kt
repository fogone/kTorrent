package ru.nobirds.torrent.client

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path
import java.nio.file.Paths

@ConfigurationProperties("client")
public class ClientProperties(
        val threads:Int = 10,
        val ports:LongRange = 6881L..6889L,
        val dhtPorts:LongRange = 11111L..11119L,
        val directory:Path = Paths.get("")
)