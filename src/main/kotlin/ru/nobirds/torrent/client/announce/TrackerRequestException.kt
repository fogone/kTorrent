package ru.nobirds.torrent.client.announce

import ru.nobirds.torrent.client.model.Torrent

public open class TrackerRequestException(reason:String) : RuntimeException(reason)