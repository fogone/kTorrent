package ru.nobirds.torrent.utils

import java.math.BigInteger
import java.security.SecureRandom

public data class Id(val size:Int = 20, factory:(Int)->Byte) {

    private val bytes = ByteArray(size).fillWith(factory)

    public fun xor(k:Id):Id = Id { bytes[it] xor k.bytes[it] }

    public fun toBytes():ByteArray = bytes.copyOf()

    public fun toBigInteger(): BigInteger = BigInteger(bytes)

    class object {
        public val Zero:Id = Id { 0 }

        private val random = SecureRandom()

        public fun random(size:Int = 20):Id = Id(size) { random.nextInt().toByte() }

        public fun fromBytes(bytes:ByteArray):Id {
            if(bytes.size != 20)
                throw IllegalArgumentException("Id must have 20 bytes length")

            return Id { bytes[it] }
        }


    }

}