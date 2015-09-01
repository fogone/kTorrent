package ru.nobirds.torrent.client.task.requirement

import ru.nobirds.torrent.client.task.state.BlockIndex

public interface TorrentRequirements {

    public fun next():BlockIndex

}