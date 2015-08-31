package ru.nobirds.torrent.utils

import java.math.BigInteger
import java.security.SecureRandom
import java.util.Arrays

public class Id(val size:Int = 20, factory:(Int)->Byte) {

    private val bytes = ByteArray(size).fillWith(factory)

    public fun xor(k:Id):Id = Id { bytes[it] xor k.bytes[it] }

    public fun toBytes():ByteArray = bytes.copyOf()

    public fun toBytes(buffer: ByteArray):ByteArray {
        bytes.copyTo(buffer)
        return buffer
    }

    public fun toBigInteger(): BigInteger = BigInteger(bytes)

    override fun equals(other: Any?): Boolean {
        if(other !is Id) return false
        if(other.size != size) return false

        return Arrays.equals(other.bytes, bytes)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(bytes)
    }

    override fun toString(): String {
        return bytes.toHexString()
    }

    companion object {
        public val Zero:Id = Id { 0 }

        private val random = SecureRandom()

        public fun random(size:Int = 20):Id = Id(size) { random.nextInt().toByte() }

        public fun fromBytes(bytes:ByteArray):Id {
            if(bytes.size() != 20)
                throw IllegalArgumentException("Id must have 20 bytes length")

            return Id { bytes[it] }
        }

        public fun fromHexString(hexString: String): Id = hexString.hexToByteArray().toId()

    }

}