package ru.nobirds.torrent.utils

import io.netty.buffer.ByteBuf
import java.math.BigInteger
import java.security.SecureRandom
import java.util.Arrays

class Id(val size:Int = 20, factory:(Int)->Byte) {

    private val bytes = ByteArray(size).fillWith(factory)

    infix fun xor(k:Id):Id = Id { bytes[it] xor k.bytes[it] }

    fun toBytes():ByteArray = bytes.copyOf()

    fun toBytes(buffer: ByteArray):ByteArray {
        bytes.copyTo(buffer)
        return buffer
    }

    fun toBigInteger(): BigInteger = BigInteger(bytes)

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
        val Zero:Id = Id { 0 }

        private val random = SecureRandom()

        fun random(size:Int = 20):Id = Id(size) { random.nextInt().toByte() }

        fun fromBytes(bytes:ByteArray):Id {
            if(bytes.size != 20)
                throw IllegalArgumentException("Id must have 20 bytes length")

            return Id { bytes[it] }
        }

        fun fromBuffer(buffer: ByteBuf):Id {
            if(buffer.readableBytes() < 20)
                throw IllegalArgumentException("Buffer must have 20 bytes or more")

            return Id { buffer.readByte() }
        }

        fun fromHexString(hexString: String): Id = hexString.hexToByteArray().toId()

    }

}