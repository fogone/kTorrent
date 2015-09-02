package ru.nobirds.torrent.client.task.requirement

import ru.nobirds.torrent.client.task.state.FreeBlockIndex
import ru.nobirds.torrent.client.task.state.TorrentState

public interface RequirementsStrategy {

    fun next(state: TorrentState, peerState: TorrentState): FreeBlockIndex?

}