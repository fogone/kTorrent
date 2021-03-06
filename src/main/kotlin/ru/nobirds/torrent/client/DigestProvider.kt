package ru.nobirds.torrent.client


import ru.nobirds.torrent.client.task.file.CompositeRandomAccessFile
import ru.nobirds.torrent.utils.equalsArray
import java.security.MessageDigest
import java.util.ArrayList
import java.util.BitSet

class DigestProvider(val digestFactory:()-> MessageDigest) {

    private val MAX_BUFFER_SIZE = 16 * 1024

    fun encode(bytes:ByteArray):ByteArray {
        return digestFactory().digest(bytes)!!
    }

    fun checkHashes(pieceLength:Long, hashes:List<ByteArray>, files:CompositeRandomAccessFile):BitSet {
        val result = BitSet(hashes.size)

        val fileHashes = calculateHashes(files, pieceLength)

        for (i in 0..hashes.size - 1) {
            result.set(i, hashes[i].equalsArray(fileHashes[i]))
        }

        return result
    }

    fun calculateHashes(files:CompositeRandomAccessFile, pieceLength:Long):List<ByteArray> {
        val result = ArrayList<ByteArray>()

        val length = files.length

        val count:Long = (length + pieceLength - 1) / pieceLength

        val buffer = ByteArray(if(pieceLength < MAX_BUFFER_SIZE) pieceLength.toInt() else MAX_BUFFER_SIZE)

        var index = 0
        var position = 0L

        for(i in 0..count-1) {
            val piece = if(position + pieceLength <= length) pieceLength else length - position

            val digest = readAndCalculateDigest(files, buffer, piece)

            result.add(digest)

            index++
            position += piece
        }

        return result
    }

    private fun readAndCalculateDigest(input:CompositeRandomAccessFile, buffer:ByteArray, length:Long):ByteArray {
        val messageDigest = digestFactory()

        val bufferSize = buffer.size.toLong()

        var position = 0L
        for (index in 0..(length/ bufferSize)-1) {
            val piece = if(position + bufferSize <= length) bufferSize else length - position

            input.read(buffer, 0, piece.toInt())

            messageDigest.update(buffer, 0, piece.toInt())

            position += piece
        }

        return messageDigest.digest()
    }
}