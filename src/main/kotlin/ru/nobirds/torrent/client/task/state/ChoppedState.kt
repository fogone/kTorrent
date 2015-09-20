package ru.nobirds.torrent.client.task.state

import ru.nobirds.torrent.client.model.TorrentInfo
import ru.nobirds.torrent.utils.copy
import ru.nobirds.torrent.utils.divToUp
import ru.nobirds.torrent.utils.setBits
import java.util.*

public interface State {

    val count: Int

    fun done()

    fun done(bits: BitSet)

    fun done(state: State)

    fun done(piece: Int)

    fun isDone():Boolean

    fun isDone(piece: Int):Boolean

    fun getBits():BitSet

    fun undone()

    fun complete():Sequence<Int>

    fun incomplete():Sequence<Int>

}

public interface IndexedState : State {

    val index:Int

}


public open class SimpleState(override val count: Int) : State {

    private val state:BitSet = BitSet(count)

    override fun done() {
        state.set(0, count)
    }

    public override fun done(bits: BitSet) {
        undone()
        bits.setBits(count).forEach { done(it) }
    }

    override fun done(piece: Int) {
        state.set(piece)
    }

    override fun isDone(piece: Int): Boolean {
        return state.get(piece)
    }

    override fun done(state: State) {
        undone()
        state.complete().forEach { this.state.set(it) }
    }

    override fun isDone(): Boolean = state.setBits(count).all { state.get(it) }

    override fun getBits(): BitSet = state.copy()

    override fun undone() {
        state.clear()
    }

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is State) return false
        return complete().toSet() == other.complete().toSet()
    }

    override fun toString(): String {
        return (0..count).joinToString(",", "[", "]") { if(isDone(it)) "1" else "0" }
    }

    override fun complete(): Sequence<Int> {
        var index = 0
        return sequence {
            val next = state.nextSetBit(index)
            if(next != -1) next else null
        }
    }

    override fun incomplete(): Sequence<Int> {
        val complete = complete().toSet()
        return (0..count).asSequence().filter { it !in complete }
    }

}

public class SimpleIndexedState(count:Int, override val index:Int) : SimpleState(count), IndexedState

public class ChoppedState(val torrentInfo:TorrentInfo, val blockLength:Int = 16 * 1024) : State {

    private val length = torrentInfo.files.totalLength

    private val pieceLength = torrentInfo.pieceLength.toInt()

    private val blocksInPiece = pieceLength.divToUp(blockLength).toInt()

    private val commonLastBlockLength = pieceLength - ((blocksInPiece-1) * blockLength)

    public override val count: Int
        get() = torrentInfo.pieceCount

    private val commonBlocksCount = (blocksInPiece * (count - 1)).toInt()

    private val commonBlocksLength = (pieceLength * (count - 1)).toInt()

    private val lastPieceBlocksLength = (length - commonBlocksLength).toInt()

    private val lastPieceBlocksCount = lastPieceBlocksLength.divToUp(blockLength).toInt()

    private val lastBlockLength = lastPieceBlocksLength - ((lastPieceBlocksCount-1) * blockLength)

    public val blocksCount:Int = (commonBlocksCount + lastPieceBlocksCount).toInt()

    private val blocksState:Array<IndexedState> = Array(torrentInfo.pieceCount) {
        if(it < count)
            SimpleIndexedState(blocksInPiece.toInt(), it)
        else
            SimpleIndexedState(lastPieceBlocksCount.toInt(), it)
    }

    public fun piece(piece: Int):IndexedState = blocksState[piece]

    public fun pieces():Sequence<IndexedState> = blocksState.asSequence()

    public override fun isDone(piece: Int):Boolean = piece(piece).isDone()

    public fun isDone(piece:Int, block:Int):Boolean = piece(piece).isDone(block)

    override fun isDone(): Boolean = blocksState.all { it.isDone() }

    public fun done(piece:Int, block:Int) {
        piece(piece).done(block)
    }

    public override fun done(bits: BitSet) {
        blocksState.forEachIndexed { i, blockState -> if(bits.get(i)) blockState.done() else blockState.undone() }
    }

    override fun done(state: State) {
        undone()
        state.complete().forEach { done(it) }
    }

    override fun done() {
        blocksState.forEach { it.done() }
    }

    override fun done(piece: Int) {
        piece(piece).done()
    }

    public fun getIndex(piece: Int, block: Int):FreeBlockIndex {

        val length = when {
            piece == count && block == lastPieceBlocksCount -> lastBlockLength
            piece != count && block == commonBlocksCount -> commonLastBlockLength
            else -> blockLength
        }

        return FreeBlockIndex(piece, block*blockLength, length)
    }

    override fun getBits(): BitSet {
        val bitSet = BitSet(count)
        blocksState.forEachIndexed { i, state -> bitSet.set(i, state.isDone()) }
        return bitSet
    }

    override fun undone() {
        blocksState.forEach { it.undone() }
    }

    override fun complete(): Sequence<Int> = blocksState.asSequence().filter { it.isDone() }.map { it.index }

    override fun incomplete(): Sequence<Int> = blocksState.asSequence().filter { !it.isDone() }.map { it.index }

}

