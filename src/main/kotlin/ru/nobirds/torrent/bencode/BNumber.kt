package ru.nobirds.torrent.bencode

import java.math.BigInteger


class BNumber() : AbstractBlockBType('i'), BValueType<BigInteger> {

    private var builder = StringBuilder()

    override val value: BigInteger
        get() = BigInteger(builder.toString())

    fun set(number:BigInteger):BNumber {
        builder = StringBuilder(number.toString())
        return this
    }

    override fun onChar(stream: BTokenStream) {
        val value = stream.currentChar().toString().toInt()
        builder.append(value)
    }

}