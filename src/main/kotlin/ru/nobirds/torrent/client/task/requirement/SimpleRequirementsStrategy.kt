package ru.nobirds.torrent.client.task.requirement

import ru.nobirds.torrent.client.task.state.*
import java.util.HashSet
import java.util.concurrent.Semaphore
import ru.nobirds.torrent.utils.copyTo
import ru.nobirds.torrent.utils.findIndex

public class SimpleRequirementsStrategy() : RequirementsStrategy {

    public override fun next(state: ChoppedState, peerState: State): FreeBlockIndex? {
        if(state.isDone())
            return null

        val piece = state.find { i, state -> !state }
        val blockState = state.state(piece!!)
        val block = blockState.find { i, state -> !state }!!

        return state.getIndex(piece, block)
    }

}