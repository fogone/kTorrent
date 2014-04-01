package ru.nobirds.torrent.kademlia.message

public trait RequestContainer {

    fun findById(id:String):RequestMessage?

    fun removeById(id:String)

    fun storeWithTimeout(request:RequestMessage, timeout:Long = 100000L, timeoutListener:(RequestMessage)->Unit)

}