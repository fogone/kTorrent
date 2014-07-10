package ru.nobirds.torrent.kademlia

import ru.nobirds.torrent.utils.fillWith
import ru.nobirds.torrent.utils.xor
import java.security.SecureRandom
import java.math.BigInteger


public data class Id(factory:(Int)->Byte) {

    public val size:Int = 20

    private val bytes = ByteArray(size).fillWith(factory)

    public fun xor(k:Id):Id = Id { bytes[it] xor k.bytes[it] }

    public fun toBytes():ByteArray = bytes.copyOf()

    public fun toBigInteger():BigInteger = BigInteger(bytes)

    class object {
        public val Zero:Id = Id { 0 }

        private val random = SecureRandom()

        public fun random():Id = Id { random.nextInt().toByte() }

        public fun fromBytes(bytes:ByteArray):Id {
            if(bytes.size != 20)
                throw IllegalArgumentException("Id must have 20 bytes length")

            return Id { bytes[it] }
        }


    }

}