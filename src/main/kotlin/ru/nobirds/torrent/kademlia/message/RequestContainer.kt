package ru.nobirds.torrent.kademlia.message

public trait RequestContainer {

    fun findById(id:Long):RequestMessage?

    fun removeById(id:Long)

    fun storeWithTimeout(request:RequestMessage, timeout:Long = 100000L)

}