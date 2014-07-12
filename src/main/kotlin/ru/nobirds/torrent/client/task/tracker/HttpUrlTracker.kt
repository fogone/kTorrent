package ru.nobirds.torrent.client.task.tracker

import java.util.ArrayList
import java.net.URL
import java.util.Timer
import java.util.TimerTask
import ru.nobirds.torrent.client.announce.UpdateAnnounceActor
import ru.nobirds.torrent.client.task.TorrentTask
import ru.nobirds.torrent.client.announce.InfoHashNotFoundException
import org.springframework.web.client.HttpServerErrorException
import ru.nobirds.torrent.client.Peer
import ru.nobirds.torrent.client.announce.TrackerInfoMessage

public class HttpUrlTracker(val timer:Timer, val announceService: UpdateAnnounceActor, val task:TorrentTask, val url:URL) : Tracker {

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

    private fun setStatus(status:TrackerStatus) {
        statusImpl = status
    }

    private fun updatePeersAndInterval() {
        /*try {
            setStatus(TrackerStatus.working)

            val trackerInfo = TrackerInfoMessage() // announceService.getTrackerInfoByUrl(task, url)

            if(this.interval != trackerInfo.interval) {
                updateInterval(interval)
            }

            if(!trackerInfo.peers.isEmpty()) {
                notifyListeners(trackerInfo.peers)
            }

            setStatus(TrackerStatus.waiting)
        } catch(e: Exception) {
            e.printStackTrace() // todo
            setStatus(exceptionMapper.map(e))
        }*/
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

