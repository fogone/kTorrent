package ru.nobirds.torrent.client.task.requirement

import ru.nobirds.torrent.client.task.state.TorrentState
import ru.nobirds.torrent.client.task.state.BlockIndex
import ru.nobirds.torrent.client.task.state.StateListener
import java.util.HashSet
import java.util.concurrent.Semaphore
import ru.nobirds.torrent.utils.copy
import ru.nobirds.torrent.utils.findIndex

public class SimpleRequirementsStrategy() : RequirementsStrategy {

    public override fun next(state:TorrentState, peerState:TorrentState):BlockIndex? {
        val bitSet = peerState.toBitSet().copy()
        bitSet.andNot(state.toBitSet())
        val piece = bitSet.nextSetBit(fromPiece)

        if(piece == -1)
            return null

        val blocksBitSet = peerState.toBitSet(piece).copy()

        blocksBitSet.andNot(state.toBitSet(piece))

        val block = blocksBitSet.findIndex(state.pieceLength.toInt(), true)
                        { BlockIndex(piece, it) !in values }

        if(block == -1)
            return findFirstUndone(fromPiece+1)

        return BlockIndex(piece, block)
    }

}