package ru.nobirds.torrent.dht.message

interface RequestContainer {

    fun findById(id:String):RequestMessage?

    fun removeById(id:String):RequestMessage?

    fun storeWithTimeout(request:RequestMessage, timeout:Long = 100000L, timeoutListener:(RequestMessage)->Unit)

    fun cancelById(id: String)

}