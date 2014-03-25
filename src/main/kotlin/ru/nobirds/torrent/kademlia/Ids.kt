package ru.nobirds.torrent.kademlia

import java.security.SecureRandom

public object Ids {

    private val random = SecureRandom()

    public fun random():Id = Id { random.nextInt().toByte() }

    public fun bytes(bytes:ByteArray):Id {
        if(bytes.size != 16)
            throw IllegalArgumentException("Bytes length = ${bytes.size} != 16")

        return Id { bytes[it] }
    }
}