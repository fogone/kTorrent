package ru.nobirds.torrent.client.task.state

import java.util.BitSet
import ru.nobirds.torrent.client.model.TorrentInfo
import java.util.ArrayList
import ru.nobirds.torrent.utils.divToUp
import ru.nobirds.torrent.utils.isAllSet
import ru.nobirds.torrent.utils.setAll
import ru.nobirds.torrent.utils.eachSet

public class TorrentState(val torrentInfo:TorrentInfo, val blockLength:Long = 16L * 1024L) {

    private val length = torrentInfo.files.totalLength

    val pieceLength = torrentInfo.pieceLength

    private val blocksInPiece = pieceLength.divToUp(blockLength).toInt()

    private val commonLastBlockLength = pieceLength - ((blocksInPiece-1) * blockLength)

    public val piecesCount:Int = torrentInfo.pieceCount

    private val commonBlocksCount = (blocksInPiece * (piecesCount - 1)).toInt()

    private val commonBlocksLength = (pieceLength * (piecesCount - 1))

    private val lastPieceBlocksLength = length - commonBlocksLength

    private val lastPieceBlocksCount = lastPieceBlocksLength.divToUp(blockLength).toInt()

    private val lastBlockLength = lastPieceBlocksLength - ((lastPieceBlocksCount-1) * blockLength)

    public val blocksCount:Int = (commonBlocksCount + lastPieceBlocksCount).toInt()

    private val piecesState = BitSet(torrentInfo.pieceCount)

    private val blocksState:Array<BitSet> = Array(torrentInfo.pieceCount) {
        if(it < piecesCount)
            BitSet(blocksInPiece.toInt())
        else
            BitSet(lastPieceBlocksCount.toInt())
    }

    private val listeners = ArrayList<StateListener>()

    public fun registerListener(listener:StateListener) {
        listeners.add(listener)
    }

    private fun firePieceComplete(piece:Int) {
        for (listener in listeners) {
            listener.onPieceComplete(piece)
        }
    }

    private fun fireBlockComplete(piece:Int, block:Int) {
        for (listener in listeners) {
            listener.onBlockComplete(piece, block)
        }
    }

    public fun blockToIndex(block:Int): BlockIndex {
        val piece = block / blocksInPiece
        return BlockIndex(piece.toInt(), block - (piece*blocksInPiece).toInt())
    }

    public fun blockIndexToFreeBlockIndex(piece:Int, block:Int):FreeBlockIndex {
        val begin = block * blockLength
        val length = blockLength(piece, block)
        return FreeBlockIndex(piece, begin.toInt(), length.toInt())
    }

    public fun blockIndexToGlobalIndex(piece:Int, block:Int):GlobalBlockIndex {
        val freeBlockIndex = blockIndexToFreeBlockIndex(piece, block)
        return freeIndexToGlobalIndex(freeBlockIndex.piece, freeBlockIndex.begin, freeBlockIndex.length)
    }

    public fun freeIndexToGlobalIndex(piece:Int, begin:Int, length:Int):GlobalBlockIndex {
        return GlobalBlockIndex((piece * pieceLength).toInt() + begin, length)
    }

    public fun freeIndexToBlockIndex(piece:Int, begin:Int, length:Int):BlockIndex? {
        if(blockLength != length.toLong())
            return null

        if(begin % blockLength.toInt() != 0)
            return null

        val block = begin / blockLength.toInt()

        return BlockIndex(piece, block)
    }

    public fun blockLength(piece:Int, block:Int):Long {
        if(piece == piecesCount-1 && block == lastPieceBlocksCount - 1)
            return lastBlockLength

        if(block == blocksInPiece - 1)
            return commonLastBlockLength

        return blockLength
    }

    public fun isBlockDone(block:Int):Boolean {
        val index = blockToIndex(block)
        return isDone(index.piece, index.block)
    }

    public fun isDone(piece:Int, block:Int):Boolean = blocksState[piece].get(block)

    public fun isDone(piece:Int):Boolean = piecesState.get(piece)

    public fun isDone():Boolean = piecesState.isAllSet(piecesCount)

    public fun done(piece:Int, block:Int, value:Boolean = true) {
        val pieceState = blocksState[piece]

        val blockStateValue = pieceState.get(block)
        pieceState.set(block, value)

        if(value && value != blockStateValue)
            fireBlockComplete(piece, block)

        val pieceStateValue = pieceState.isAllSet(blocksInPiece)

        if(pieceStateValue) {
            firePieceComplete(piece)
        }

        piecesState.set(piece, pieceStateValue) // todo
    }

    public fun done(piece:Int, value:Boolean = true) {
        blocksState[piece].setAll(blocksInPiece, value)
        piecesState.set(piece, value)

        if(value)
            firePieceComplete(piece)
    }

    public fun done(set:BitSet) {
        piecesState.setAll(piecesCount, false)
        blocksState.forEach { it.setAll(it.size(), false) }

        set.eachSet(piecesCount) { value, index ->
            done(index)
            value
        }
    }

    public fun toBitSet(piece:Int):BitSet = blocksState[piece]
    public fun toBitSet():BitSet = piecesState
}