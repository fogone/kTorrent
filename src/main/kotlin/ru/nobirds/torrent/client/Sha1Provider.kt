package ru.nobirds.torrent.client

import org.springframework.stereotype.Service as service
import java.security.MessageDigest
import ru.nobirds.torrent.toHexString
import java.util.BitSet
import ru.nobirds.torrent.client.task.CompositeFileDescriptor
import ru.nobirds.torrent.client.task.file.CompositeRandomAccessFile
import java.io.DataInput

public object Sha1Provider {

    private val MAX_BUFFER_SIZE = 16 * 1024

    public fun encode(bytes:ByteArray):String {
        return createDigest()
                .digest(bytes)!!
                .toHexString()
    }

    public fun createDigest():MessageDigest = MessageDigest.getInstance("SHA-1")

    public fun checkHashes(hashes:List<String>, files:CompositeRandomAccessFile):BitSet {
        val result = BitSet(hashes.size)

        val length = files.length

        val pieceLength = length / hashes.size

        val buffer = ByteArray(if(pieceLength < MAX_BUFFER_SIZE) pieceLength.toInt() else MAX_BUFFER_SIZE)

        var index = 0
        var position = 0L
        for (hash in hashes) {
            val piece = if(position + pieceLength <= length) pieceLength else length - position

            val digest = readAndCalculateDigest(files.input, buffer, piece)

            val checked = digest.equals(hash)
            result.set(index, checked)

            index++
            position += piece
        }

        return result
    }

    private fun readAndCalculateDigest(input:DataInput, buffer:ByteArray, length:Long):String {
        val messageDigest = createDigest()

        val bufferSize = buffer.size.toLong()

        var position = 0L
        for (index in 0..(length/ bufferSize)-1) {
            val piece = if(position + bufferSize <= length) bufferSize else length - position

            input.readFully(buffer, 0, piece.toInt())

            messageDigest.update(buffer, 0, piece.toInt())

            position += piece
        }

        return messageDigest.digest()!!.toHexString()
    }
}