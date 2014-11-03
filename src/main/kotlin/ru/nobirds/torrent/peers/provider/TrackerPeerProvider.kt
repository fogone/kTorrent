package ru.nobirds.torrent.peers.provider

import java.util.ArrayList
import java.util.TimerTask
import java.util.Timer
import java.util.concurrent.ConcurrentHashMap
import ru.nobirds.torrent.announce.HttpAnnounceProvider
import ru.nobirds.torrent.announce.UdpAnnounceProvider
import ru.nobirds.torrent.utils.Id
import kotlin.concurrent.timerTask
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import ru.nobirds.torrent.announce.TrackerRequestException
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.peers.PeerEvent
import java.util.concurrent.CopyOnWriteArrayList
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import ru.nobirds.torrent.announce.InfoHashNotFoundException

public enum class TrackerStatus() {

    notChecked
    waiting
    working
    notFound
    error

}

data class Tracker(val uri: URI, var status: TrackerStatus = TrackerStatus.notChecked)

public class TrackerPeerProvider(localPeer: Peer) : AbstractPeerProvider(localPeer) {

    private val httpAnnounceProvider = HttpAnnounceProvider()

    private val announceProviders = hashMapOf(
            "http" to httpAnnounceProvider,
            "https" to httpAnnounceProvider,
            "udp" to UdpAnnounceProvider()
    )

    private val defaultInterval = 10L * 60L * 1000L

    private val timer = Timer()

    private val tasks = ConcurrentHashMap<Id, TimerTask>()

    private val trackers = CopyOnWriteArrayList<Tracker>()

    private val workingStatuses = hashSetOf(TrackerStatus.working, TrackerStatus.notChecked)

    public fun registerTracker(uri: String) {
        val trackerUri = UriComponentsBuilder.fromUriString(uri).build().toUri()
        trackers.add(Tracker(trackerUri))
    }

    private fun updatePeersAndInterval(hash: Id) {
        trackers
                .filter { tracker -> tracker.status in workingStatuses }
                .forEach { tracker ->
                    try {
                        processTracker(hash, tracker)
                    } catch(e: Exception) {
                        tracker.status = mapException(e)
                    }
                }
    }

    private fun mapException(e:Exception):TrackerStatus = when(e) {
        is InfoHashNotFoundException -> TrackerStatus.notFound
        is HttpServerErrorException ->
            if(e.getStatusCode() == HttpStatus.NOT_FOUND) TrackerStatus.notFound else TrackerStatus.error
        else -> TrackerStatus.error
    }

    private fun processTracker(hash: Id, tracker: Tracker) {
        val announceProvider = announceProviders[tracker.uri.getScheme()]
        if (announceProvider == null)
            throw RuntimeException("Schema ${tracker.uri.getScheme()} not supported.")

        val trackerInfo = announceProvider.getTrackerInfoByUrl(tracker.uri, localPeer, hash)

        if (trackerInfo.peers.notEmpty)
            notifyPeerEvent(PeerEvent(hash, trackerInfo.peers.map { it.address }.toSet()))

        tracker.status = TrackerStatus.working
    }

    override fun onHashRequired(hash: Id) {
        cancelTimer(hash)
        updatePeersAndInterval(hash)
        bindTimer(hash)
    }

    override fun onNoHashNeeded(hash: Id) {
        cancelTimer(hash)
    }

    private fun bindTimer(hash: Id) {
        val timerTask = timerTask { updatePeersAndInterval(hash) }
        tasks.put(hash, timerTask)
        timer.schedule(timerTask, defaultInterval)
    }

    private fun cancelTimer(hash: Id) {
        tasks.remove(hash)?.cancel()
    }

}