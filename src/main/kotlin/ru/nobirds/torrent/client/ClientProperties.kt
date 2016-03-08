package ru.nobirds.torrent.client

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path
import java.nio.file.Paths

@ConfigurationProperties("client") class ClientProperties(
        val ports:IntRange = 6881..6889,
        val dhtPorts:IntRange = 11111..11119,
        val directory:Path = Paths.get("")
)