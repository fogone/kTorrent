package ru.nobirds.torrent.kademlia.message

import java.util.concurrent.ConcurrentHashMap

public class DefaultRequestContainer() : RequestContainer {

    private val storage = ConcurrentHashMap<Long, RequestMessage>()

    override fun findById(id: Long): RequestMessage? = storage[id]

    override fun removeById(id: Long) {
        storage.remove(id)
    }

    override fun storeWithTimeout(request: RequestMessage, timeout: Long) {
        storage[request.id] = request
    }

}