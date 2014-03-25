package ru.nobirds.torrent.kademlia.message

public trait RequestContainer {

    fun findById(id:Long):RequestMessage?

    fun storeWithTimeout(request:RequestMessage, timeout:Long)

}