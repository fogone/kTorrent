package ru.nobirds.torrent.client.task.requirement

import ru.nobirds.torrent.client.task.state.TorrentState
import ru.nobirds.torrent.client.task.state.BlockIndex
import ru.nobirds.torrent.client.task.state.StateListener
import java.util.HashSet
import java.util.concurrent.Semaphore
import ru.nobirds.torrent.utils.copy
import ru.nobirds.torrent.utils.findIndex

public class SimpleTorrentRequirements(val state:TorrentState, val peerState:TorrentState) : TorrentRequirements {

    private val values = HashSet<BlockIndex>()

    private val lock = Semaphore(1)

    init { registerListeners() }

    private fun registerListeners() {
        state.registerListener(object : StateListener {
            override fun onPieceComplete(piece: Int) {
                // todo: values.remove { it.piece == piece }
            }
            override fun onBlockComplete(piece: Int, block: Int) {
                // todo: values.remove { it.piece == piece && it.block == block}
            }
        })

        peerState.registerListener(object : StateListener {
            override fun onPieceComplete(piece: Int) {
                lock.release()
            }
            override fun onBlockComplete(piece: Int, block: Int) {
                // not used
            }
        })
    }

    override fun next(): BlockIndex {
        val blockIndex = findFirstUndone()

        if(blockIndex != null)
            return blockIndex

        lock.acquire()

        return next()
    }

    private fun findFirstUndone(fromPiece:Int = 0):BlockIndex? {
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