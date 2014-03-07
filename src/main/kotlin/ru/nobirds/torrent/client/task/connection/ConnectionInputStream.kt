package ru.nobirds.torrent.client.task.connection

import java.io.FilterInputStream
import java.io.InputStream

class ConnectionClosedException() : RuntimeException("Connection closed")

class ConnectionInputStream(val stream:InputStream) : FilterInputStream(stream) {

    public override fun read():Int {
        val value = stream.read()

        if(value == -1)
            throw ConnectionClosedException()

        return value
    }

}

