package ru.nobirds.torrent.client

import org.springframework.stereotype.Service as service
import java.security.MessageDigest
import ru.nobirds.torrent.toHexString

public service class Sha1Service {

    public fun encode(bytes:ByteArray):String {
        return MessageDigest
                .getInstance("SHA-1")
                .digest(bytes)!!
                .toHexString()
    }

}