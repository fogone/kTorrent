package ru.nobirds.torrent.client.announce

import ru.nobirds.torrent.client.model.Torrent

public class TorrentNotFoundException(torrent:Torrent) : RuntimeException("Torrent not found")