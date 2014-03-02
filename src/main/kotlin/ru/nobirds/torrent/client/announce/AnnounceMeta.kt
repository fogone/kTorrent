package ru.nobirds.torrent.client.announce

import java.net.URL
import java.util.Timer
import java.util.TimerTask
import ru.nobirds.torrent.client.Peer
import java.util.ArrayList
import java.util.concurrent.CopyOnWriteArrayList

public class AnnounceMeta(val url:URL, val timer:Timer, var interval:Long) {

    val listeners:MutableList<AnnounceListener> = CopyOnWriteArrayList()

    var timerTask = createAndScheduleTask(interval)

    private fun createAndScheduleTask(interval:Long):TimerTask {
        val task = createTask()
        this.interval = interval
        if(interval > 0)
            timer.schedule(task, 0, interval)
        return task
    }

    private fun createTask():TimerTask = object : TimerTask() {
        override fun run() {
            for (listener in listeners) {
                listener.onSchedule(url)
            }
        }
    }

    public fun renewInterval(interval:Long) {
        if(this.interval != interval) {
            this.timerTask.cancel()
            this.timerTask = createAndScheduleTask(interval)
        }
    }

    public fun registerListener(listener:AnnounceListener) {
        if(!listeners.contains(listener))
            listeners.add(listener)
    }

    public fun unregisterListener(listener:AnnounceListener) {
        listeners.remove(listener)
    }

}