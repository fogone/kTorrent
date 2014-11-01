package ru.nobirds.torrent.announce

import ru.nobirds.torrent.client.model.Torrent

public class InfoHashNotFoundException() : TrackerRequestException("Torrent not found")