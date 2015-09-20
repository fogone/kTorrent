package ru.nobirds.torrent.dht.message

import ru.nobirds.torrent.utils.scheduleOnce
import java.util.*
import java.util.concurrent.ConcurrentHashMap


public class DefaultRequestContainer() : RequestContainer {

    private data class ContainerSlot(val request:RequestMessage, val task:TimerTask)

    private val timer = Timer("Request container timer")

    private val storage = ConcurrentHashMap<String, ContainerSlot>()

    override fun findById(id: String): RequestMessage? = storage[id]?.request

    override fun removeById(id: String):RequestMessage? {
        val slot = storage.remove(id)

        if(slot != null) {
            slot.task.cancel()
            return slot.request
        }

        return null
    }

    override fun storeWithTimeout(request: RequestMessage, timeout: Long, timeoutListener:(RequestMessage)->Unit) {
        // todo: check request.id exists

        val task = timer.scheduleOnce(timeout) {
            removeById(request.id)
            timeoutListener(request)
        }

        storage[request.id] = ContainerSlot(request, task)
    }

    override fun cancelById(id: String) {
        val slot = storage.get(id)
        if(slot != null)
            slot.task.cancel()
    }

}