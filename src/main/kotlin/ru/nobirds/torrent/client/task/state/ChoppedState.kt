package ru.nobirds.torrent.client.task.state

import java.util.BitSet
import ru.nobirds.torrent.client.model.TorrentInfo
import ru.nobirds.torrent.utils.*
import java.util.ArrayList

public interface State {

    val count:Int

    fun done()

    fun done(bytes: ByteArray)

    fun done(piece: Int)

    fun isDone():Boolean

    fun isDone(piece: Int):Boolean

    fun toBytes():ByteArray

}

public fun State.find(condition:(Int, Boolean)->Boolean):Int? {
    for (i in 0..count) {
        if(condition(i, isDone(i)))
            return i
    }

    return null
}

public class SimpleState(override val count:Int) : State {

    private val state:BitSet = BitSet(count)

    override fun done() {
        state.setAll(count, true)
    }

    public override fun done(bytes:ByteArray) {
        BitSet.valueOf(bytes).copyTo(this.state, count)
    }

    override fun done(piece: Int) {
        state.set(piece)
    }

    override fun isDone(piece: Int): Boolean {
        return state.get(piece)
    }

    override fun isDone(): Boolean = state.isAllSet(count)

    override fun toBytes(): ByteArray = state.toByteArray()

}

public class ChoppedState(val torrentInfo:TorrentInfo, val blockLength:Int = 16 * 1024) : State {

    private val length = torrentInfo.files.totalLength

    private val pieceLength = torrentInfo.pieceLength.toInt()

    private val blocksInPiece = pieceLength.divToUp(blockLength).toInt()

    private val commonLastBlockLength = pieceLength - ((blocksInPiece-1) * blockLength)

    override val count: Int
        get() = torrentInfo.pieceCount

    private val commonBlocksCount = (blocksInPiece * (count - 1)).toInt()

    private val commonBlocksLength = (pieceLength * (count - 1)).toInt()

    private val lastPieceBlocksLength = (length - commonBlocksLength).toInt()

    private val lastPieceBlocksCount = lastPieceBlocksLength.divToUp(blockLength).toInt()

    private val lastBlockLength = lastPieceBlocksLength - ((lastPieceBlocksCount-1) * blockLength)

    public val blocksCount:Int = (commonBlocksCount + lastPieceBlocksCount).toInt()

    private val blocksState:Array<State> = Array(torrentInfo.pieceCount) {
        if(it < count)
            SimpleState(blocksInPiece.toInt())
        else
            SimpleState(lastPieceBlocksCount.toInt())
    }

    public fun state(piece: Int):State = blocksState[piece]

    public override fun isDone(piece: Int):Boolean = state(piece).isDone()

    public fun isDone(piece:Int, block:Int):Boolean = state(piece).isDone(block)

    override fun isDone(): Boolean = blocksState.all { it.isDone() }

    public fun done(piece:Int, block:Int) {
        state(piece).done(block)
    }

    public override fun done(bytes:ByteArray) {
        val state = BitSet.valueOf(bytes)
        blocksState.forEachIndexed { i, blockState -> if(state.get(i)) blockState.done() }
    }

    override fun done() {
        blocksState.forEach { it.done() }
    }

    override fun done(piece: Int) {
        state(piece).done()
    }

    public fun getIndex(piece: Int, block: Int):FreeBlockIndex {

        val length = when {
            piece == count && block == lastPieceBlocksCount -> lastBlockLength
            piece != count && block == commonBlocksCount -> commonLastBlockLength
            else -> blockLength
        }

        return FreeBlockIndex(piece, block*blockLength, length)
    }

    override fun toBytes(): ByteArray {
        val bitSet = BitSet(count)
        blocksState.forEachIndexed { i, state -> bitSet.set(i, state.isDone()) }
        return bitSet.toByteArray()
    }
}
