package ru.nobirds.torrent.client.task.state

data class BlockPositionAndBytes(val position: BlockPosition, val block:ByteArray)
data class BlockPosition(val piece:Int, val begin:Int)
data class BlockPositionAndSize(val position:BlockPosition, val size:Int) {
    fun withBytes(bytes:ByteArray):BlockPositionAndBytes {
        require(bytes.size == size) { "Bytes count ${bytes.size} not equals to block size $size" }
        return BlockPositionAndBytes(position, bytes)
    }
}

data class GlobalBlockPositionAndSize(val begin:Int, val length:Int)

object Blocks {
    fun position(piece:Int, begin:Int):BlockPosition = BlockPosition(piece, begin)
    fun positionAndSize(piece:Int, begin:Int, size:Int):BlockPositionAndSize = BlockPositionAndSize(BlockPosition(piece, begin), size)
    fun positionAndBytes(piece:Int, begin: Int, block: ByteArray): BlockPositionAndBytes = BlockPositionAndBytes(BlockPosition(piece, begin), block)
}