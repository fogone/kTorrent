package ru.nobirds.torrent.peers.provider

import ru.nobirds.torrent.dht.Dht
import ru.nobirds.torrent.peers.PeerEvent
import ru.nobirds.torrent.utils.Id
import java.util.concurrent.*

class DhtPeerProvider(val dht:Dht) : AbstractPeerProvider() {

    private val scheduler:ScheduledExecutorService = ScheduledThreadPoolExecutor(10)

    private val scheduledHashes = ConcurrentHashMap<Id, ScheduledFuture<*>>()

    override fun onHashRequired(hash: Id) {
        dht.announce(hash)

        val scheduledFuture = scheduler.scheduleAtFixedRate(
                { if(!dht.hasPeersFor(hash)) requestFiendPeers(hash) }, 0, 10, TimeUnit.SECONDS)

        scheduledHashes.put(hash, scheduledFuture)
    }

    private fun requestFiendPeers(hash: Id) {
        dht.findPeersForHash(hash) { node ->
            notifyPeerEvent(PeerEvent(hash, hashSetOf(node)))
        }
    }

    override fun onNoHashNeeded(hash: Id) {
        scheduledHashes.get(hash)?.cancel(false)
    }

}