package ru.nobirds.torrent.client


import java.security.MessageDigest
import ru.nobirds.torrent.toHexString
import java.util.BitSet
import ru.nobirds.torrent.client.task.file.CompositeRandomAccessFile
import java.io.DataInput
import java.util.Arrays
import java.util.ArrayList
import ru.nobirds.torrent.equalsArray

public object Sha1Provider {

    private val MAX_BUFFER_SIZE = 16 * 1024

    public fun encode(bytes:ByteArray):ByteArray {
        return createDigest().digest(bytes)!!
    }

    public fun createDigest():MessageDigest = MessageDigest.getInstance("SHA-1")

    public fun checkHashes(pieceLength:Long, hashes:List<ByteArray>, files:CompositeRandomAccessFile):BitSet {
        val result = BitSet(hashes.size)

        val fileHashes = calculateHashes(files, pieceLength)

        for (i in 0..hashes.size - 1) {
            result.set(i, hashes[i].equalsArray(fileHashes[i]))
        }

        return result
    }

    public fun calculateHashes(files:CompositeRandomAccessFile, pieceLength:Long):List<ByteArray> {
        val result = ArrayList<ByteArray>()

        val length = files.length

        val count:Long = (length + pieceLength - 1) / pieceLength

        val buffer = ByteArray(if(pieceLength < MAX_BUFFER_SIZE) pieceLength.toInt() else MAX_BUFFER_SIZE)

        var index = 0
        var position = 0L

        for(i in 0..count-1) {
            val piece = if(position + pieceLength <= length) pieceLength else length - position

            val digest = readAndCalculateDigest(files.input, buffer, piece)

            result.add(digest)

            index++
            position += piece
        }

        return result
    }

    private fun readAndCalculateDigest(input:DataInput, buffer:ByteArray, length:Long):ByteArray {
        val messageDigest = createDigest()

        val bufferSize = buffer.size.toLong()

        var position = 0L
        for (index in 0..(length/ bufferSize)-1) {
            val piece = if(position + bufferSize <= length) bufferSize else length - position

            input.readFully(buffer, 0, piece.toInt())

            messageDigest.update(buffer, 0, piece.toInt())

            position += piece
        }

        return messageDigest.digest()!!
    }
}