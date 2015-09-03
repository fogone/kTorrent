package ru.nobirds.torrent.client.task.requirement

import ru.nobirds.torrent.client.task.state.ChoppedState
import ru.nobirds.torrent.client.task.state.FreeBlockIndex
import ru.nobirds.torrent.client.task.state.State

public interface RequirementsStrategy {

    fun next(state: ChoppedState, peerState: State, count:Int): Sequence<FreeBlockIndex>

}