package ru.nobirds.torrent.client.announce

import java.net.URL
import java.util.Timer
import java.util.TimerTask
import ru.nobirds.torrent.client.Peer
import java.util.ArrayList
import java.util.concurrent.CopyOnWriteArrayList
import ru.nobirds.torrent.client.task.TorrentTask
import java.util.HashSet
import ru.nobirds.torrent.client.task.UpdatePeersMessage

public class AnnounceUpdater(val announceService:AnnounceService, val url:URL, val timer:Timer, var interval:Long = 0) {

    private val defaultUpdateInterval = 60 * 1000L

    private val tasks = HashSet<TorrentTask>()

    private var timerTask = createAndScheduleTask(interval)

    public fun registerTask(task:TorrentTask) {
        tasks.add(task)
    }

    public fun unregisterTask(task:TorrentTask) {
        tasks.remove(task)
    }

    private fun createAndScheduleTask(interval:Long):TimerTask {
        val task = createTask()
        this.interval = interval

        if(interval > 0)
            timer.schedule(task, 0, interval)
        else
            timer.schedule(task, 0, defaultUpdateInterval)

        return task
    }

    private fun createTask():TimerTask = object : TimerTask() {
        override fun run() {
            for (task in tasks) {
                task.sendMessage(UpdatePeersMessage(url, announceService.getPeersForTask(task, url)))
            }
        }
    }

    public fun renewInterval(interval:Long) {
        if(this.interval != interval) {
            this.timerTask.cancel()
            this.timerTask = createAndScheduleTask(interval)
        }
    }

}