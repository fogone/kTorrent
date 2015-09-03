package ru.nobirds.torrent.client.task.requirement

import ru.nobirds.torrent.client.task.state.ChoppedState
import ru.nobirds.torrent.client.task.state.FreeBlockIndex
import ru.nobirds.torrent.client.task.state.State
import ru.nobirds.torrent.client.task.state.incomplete

public class SimpleRequirementsStrategy() : RequirementsStrategy {

    public override fun next(state: ChoppedState, peerState: State, count:Int): Sequence<FreeBlockIndex> {
        if(state.isDone())
            return emptySequence()

        return state
                .pieces()
                .filter { !it.isDone() }
                .flatMap { piece ->
                    piece
                            .incomplete()
                            .map { state.getIndex(piece.index, it) }
                }.take(count)
    }

}