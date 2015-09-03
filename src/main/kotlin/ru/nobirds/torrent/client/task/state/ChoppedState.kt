package ru.nobirds.torrent.client.task.state

import ru.nobirds.torrent.client.model.TorrentInfo
import ru.nobirds.torrent.utils.copyTo
import ru.nobirds.torrent.utils.divToUp
import ru.nobirds.torrent.utils.isAllSet
import ru.nobirds.torrent.utils.setAll
import java.util.*
import kotlin.support.AbstractIterator

public interface State {

    val count:Int

    fun done()

    fun done(bytes: ByteArray)

    fun done(state: State)

    fun done(piece: Int)

    fun isDone():Boolean

    fun isDone(piece: Int):Boolean

    fun toBytes():ByteArray

    fun undone()

}

public interface IndexedState : State {

    val index:Int

}

public fun State.complete():Sequence<Int> = object: Sequence<Int> {
    private var index = 0

    override fun iterator(): Iterator<Int> = object: AbstractIterator<Int>() {
        override fun computeNext() {
            while (index < count && !isDone(index)) {
                index++
            }

            if(index==count) done() else setNext(index)
        }
    }
}
public fun State.incomplete():Sequence<Int> = object: Sequence<Int> {
    private var index = 0

    override fun iterator(): Iterator<Int> = object: AbstractIterator<Int>() {
        override fun computeNext() {
            while (index < count && isDone(index)) {
                index++
            }

            if(index==count) done() else setNext(index)
        }
    }
}

public open class SimpleState(override val count:Int) : State {

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

    override fun done(state: State) {
        this.state.clear()
        state.complete().forEach { this.state.set(it) }
    }

    override fun isDone(): Boolean = state.isAllSet(count)

    override fun toBytes(): ByteArray = state.toByteArray()

    override fun undone() {
        state.clear()
    }
}

public class SimpleIndexedState(count:Int, override val index:Int) : SimpleState(count), IndexedState

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

    public override fun done(bytes:ByteArray) {
        val state = BitSet.valueOf(bytes)
        blocksState.forEachIndexed { i, blockState -> if(state.get(i)) blockState.done() else blockState.undone() }
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

    override fun toBytes(): ByteArray {
        val bitSet = BitSet(count)
        blocksState.forEachIndexed { i, state -> bitSet.set(i, state.isDone()) }
        return bitSet.toByteArray()
    }

    override fun undone() {
        blocksState.forEach { it.undone() }
    }
}

