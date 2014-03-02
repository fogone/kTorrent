package ru.nobirds.torrent.client

import ru.nobirds.torrent.config.ConfigProperty
import ru.nobirds.torrent.config.Properties
import java.io.File

public object ClientProperties {

    public val peerId:ConfigProperty<String> = Properties.string("client.peer.id")
    public val clientPortsRange:ConfigProperty<LongRange> = Properties.longRange("client.ports", 6881L..6889L)

    public val torrentsDirectory:ConfigProperty<File> = Properties.directory("client.torrents.directory", File("d:\\tmp"))

}