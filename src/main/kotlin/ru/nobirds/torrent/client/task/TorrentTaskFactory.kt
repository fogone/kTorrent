package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.model.Torrent

public trait TorrentTaskFactory {

    fun create(torrent: Torrent): TorrentTask

}