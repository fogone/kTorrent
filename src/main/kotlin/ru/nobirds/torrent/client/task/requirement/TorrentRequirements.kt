package ru.nobirds.torrent.client.task.requirement

import ru.nobirds.torrent.client.task.state.TorrentState
import ru.nobirds.torrent.client.task.state.BlockIndex

public trait TorrentRequirements {

    public fun next():BlockIndex

}