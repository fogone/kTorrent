package ru.nobirds.torrent.utils

import java.util.Random
import java.math.BigInteger

object TokenGenerator {

    private val random = Random()

    fun generate():String {
        val buffer = ByteArray(16)
        random.nextBytes(buffer)
        return BigInteger(buffer).toString(16)
    }

}
