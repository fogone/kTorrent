package ru.nobirds.torrent.client.task.tracker

import java.util.ArrayList
import java.net.URL
import java.util.Timer
import java.util.TimerTask
import ru.nobirds.torrent.client.announce.AnnounceService
import ru.nobirds.torrent.client.task.TorrentTask
import ru.nobirds.torrent.client.announce.TorrentNotFoundException
import org.springframework.web.client.HttpServerErrorException
import ru.nobirds.torrent.client.Peer

public class HttpUrlTracker(val timer:Timer, val announceService:AnnounceService, val task:TorrentTask, val url:URL) : Tracker {

    private val exceptionMapper = ExceptionMapper()

    private val defaultInterval = 10L * 60L * 1000L

    private var interval:Long = defaultInterval

    private var statusImpl = TrackerStatus.notChecked

    private val listeners = ArrayList<(List<Peer>)->Unit>()

    override val status: TrackerStatus
        get() = statusImpl

    private var timerTask:TimerTask = createUpdateTrackerTask()

    ;{
        timer.schedule(timerTask, 0, interval)
    }

    private fun updateInterval(interval:Long) {
        this.timerTask.cancel()
        this.timerTask = createUpdateTrackerTask()
        this.interval = interval
        this.timer.schedule(timerTask, interval, interval)
    }

    override fun registerUpdateListener(listener: (List<Peer>) -> Unit) {
        listeners.add(listener)
    }

    private fun updatePeersAndInterval() {
        try {
            statusImpl = TrackerStatus.working

            val intervalAndPeers = announceService.getPeersByUrl(task, url)

            if(this.interval != interval) {
                updateInterval(interval)
            }

            if(!intervalAndPeers.peers.isEmpty()) {
                notifyListeners(intervalAndPeers.peers)
            }

            statusImpl = TrackerStatus.waiting
        } catch(e: Exception) {
            e.printStackTrace() // todo
            statusImpl = exceptionMapper.map(e)
        }
    }

    private fun notifyListeners(peers:List<Peer>) {
        for (listener in listeners) {
            listener(peers)
        }
    }

    private fun createUpdateTrackerTask():TimerTask = object : TimerTask() {
        override fun run() {
            updatePeersAndInterval()
        }
    }

}