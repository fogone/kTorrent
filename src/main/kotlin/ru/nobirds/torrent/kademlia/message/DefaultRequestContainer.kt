package ru.nobirds.torrent.kademlia.message

import java.util.concurrent.ConcurrentHashMap
import ru.nobirds.torrent.utils.nullOr
import java.util.Timer
import ru.nobirds.torrent.utils.scheduleOnce
import java.util.TimerTask

data class ContainerSlot(val request:RequestMessage, val task:TimerTask)

public class DefaultRequestContainer() : RequestContainer {

    private val timer = Timer("Request container timer")

    private val storage = ConcurrentHashMap<String, ContainerSlot>()

    override fun findById(id: String): RequestMessage? = storage[id].nullOr { request }

    override fun removeById(id: String) {
        val slot = storage.remove(id)

        if(slot != null)
            slot.task.cancel()
    }

    override fun storeWithTimeout(request: RequestMessage, timeout: Long, timeoutListener:(RequestMessage)->Unit) {
        // todo: check request.id exists

        val task = timer.scheduleOnce(timeout) {
            removeById(request.id)
            timeoutListener(request)
        }

        storage[request.id] = ContainerSlot(request, task)
    }

}