package ru.nobirds.torrent.client

import java.net.SocketException
import java.net.ServerSocket

private fun Int.isPortAvailable():Boolean {
    try {
        ServerSocket(this).close()
        return true
    } catch(e: SocketException) {
        return false
    }
}