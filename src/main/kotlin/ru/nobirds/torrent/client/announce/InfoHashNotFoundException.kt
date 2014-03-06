package ru.nobirds.torrent.client.announce

import ru.nobirds.torrent.client.model.Torrent

public class InfoHashNotFoundException() : TrackerRequestException("Torrent not found")