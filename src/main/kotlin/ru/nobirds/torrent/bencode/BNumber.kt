package ru.nobirds.torrent.bencode

import java.math.BigInteger


public class BNumber() : AbstractBlockBType<BigInteger>('i') {

    private val builder = StringBuilder()

    override fun onChar(stream: BTokenInputStream) {
        val value = stream.currentChar().toString().toInt()
        builder.append(value)
    }

    override fun createResult(): BigInteger = BigInteger(builder.toString())

}