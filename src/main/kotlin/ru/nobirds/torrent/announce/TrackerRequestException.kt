package ru.nobirds.torrent.announce

import ru.nobirds.torrent.client.model.Torrent

public open class TrackerRequestException(reason:String) : RuntimeException(reason)