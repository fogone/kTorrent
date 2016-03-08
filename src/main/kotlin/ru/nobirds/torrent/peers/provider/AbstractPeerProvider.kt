package ru.nobirds.torrent.peers.provider

import ru.nobirds.torrent.peers.PeerEvent
import ru.nobirds.torrent.peers.PeerListener
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.LinksCounter
import java.util.*

abstract class AbstractPeerProvider() : PeerProvider {

    private val linksCounter = LinksCounter<Id>()
    private val listeners = HashMap<Id, MutableSet<PeerListener>>()

    override fun require(hash: Id, listener: PeerListener) {
        val count = linksCounter.increase(hash)
        if (count == 1) {
            onHashRequired(hash)
        }

        listeners.getOrPut(hash) { HashSet() }.add(listener)
    }

    override fun needless(hash: Id) {
        val count = linksCounter.decrease(hash)
        if (count == 0) {
            onNoHashNeeded(hash)
            listeners.getOrDefault(hash, Collections.emptySet()).clear()
        }
    }

    protected fun notifyPeerEvent(event: PeerEvent) {
        listeners.filter { it.key == event.hash }.forEach {
            it.value.forEach { it.onPeerEvent(event) }
        }
    }

    abstract fun onHashRequired(hash: Id)

    abstract fun onNoHashNeeded(hash: Id)

}