package ru.nobirds.torrent.client.task.connection

import java.net.ServerSocket
import ru.nobirds.torrent.client.task.Connection
import java.util.ArrayList
import java.net.Socket

public trait ConnectionListener {

    fun onConnection(socket:Socket)

}

public class ConnectionAcceptor(val port:Int) : Thread("Connection acceptor") {

    private val serverSocket = ServerSocket(port)

    private val listeners = ArrayList<ConnectionListener>()

    public fun registerListener(listener:ConnectionListener) {
        listeners.add(listener)
    }

    override fun run() {
        while(!isInterrupted()) {
            val socket = serverSocket.accept()
            for (listener in listeners) {
                listener.onConnection(socket)
            }
        }
    }

}