package ru.nobirds.torrent.client.task

import java.util.BitSet
import ru.nobirds.torrent.client.model.TorrentInfo
import java.util.ArrayList
import ru.nobirds.torrent.nullOr
import ru.nobirds.torrent.isAllSet
import ru.nobirds.torrent.setAll
import ru.nobirds.torrent.eachSet

public class TorrentState(val torrentInfo:TorrentInfo) {

    private val blockSize = 16L * 1024L

    private val blocksInPiece = (torrentInfo.pieceLength + blockSize -1 ) / blockSize

    private val totalBlocks = (torrentInfo.files.totalLength + torrentInfo.pieceLength - 1)/torrentInfo.pieceLength

    private val piecesState = BitSet(torrentInfo.pieceCount)

    private val blocksState:Array<BitSet> = Array(torrentInfo.pieceCount) {
        if(blocksInPiece * it + blocksInPiece >= totalBlocks)
            BitSet(blocksInPiece.toInt())
        else
            BitSet((totalBlocks - blocksInPiece * it).toInt())
    }

    public fun isDone(piece:Int, block:Int):Boolean = blocksState[piece].get(block)

    public fun isDone(piece:Int):Boolean = piecesState.get(piece)

    public fun done(piece:Int, block:Int) {
        val pieceState = blocksState[piece]
        pieceState.set(block)
        piecesState.set(piece, pieceState.isAllSet())
    }

    public fun done(piece:Int) {
        blocksState[piece].setAll()
        piecesState.set(piece)
    }

    public fun done(set:BitSet) {
        piecesState.setAll(false)
        blocksState.forEach { it.setAll(false) }

        set.eachSet { value, index ->
            done(index)
            value
        }
    }

    public fun toBitSet():BitSet = piecesState
}