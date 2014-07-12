package ru.nobirds.torrent.client.announce

import akka.actor.UntypedActor
import java.util.Timer
import java.util.HashMap
import java.util.TimerTask
import akka.actor.ActorRef
import ru.nobirds.torrent.utils.toUrlString
import ru.nobirds.torrent.utils.toHash

public data object TimeToUpdateTrackerMessage

class Task(val torrentHash: String, val interval: Long, val listener: ActorRef, val self: ActorRef) : TimerTask() {

    override fun run() {
        listener.tell(TimeToUpdateTrackerMessage, self)
    }

    public fun withNewInterval(interval: Long): Task = Task(torrentHash, interval, listener, self)

}

public class TrackersActor() : UntypedActor() {

    private val defaultInterval = 30L * 1000L

    private val timer:Timer = Timer()

    private val timerTasks = HashMap<String, Task>()

    private fun createCompositeKey(hash:String, url: String): String = "${url}/${hash}"

    private fun sendUpdateMessage(message: UpdateAnnounceMessage) {
        context()!!
                .actorSelection("/user/announces")
                .tell(message, self())
    }

    private fun updateAnnounce(message: UpdateAnnounceMessage) {
        val hash = createCompositeKey(message.torrentHash.toUrlString(), message.url.toString().toHash())

        if (!timerTasks.containsKey(hash)) {
            val task = Task(hash, defaultInterval, sender()!!, self()!!)
            timerTasks[hash] = task
            timer.schedule(task, task.interval, task.interval)
        }

        sendUpdateMessage(message)
    }

    private fun updateTracker(message: TrackerInfoMessage) {
        val hash = message.hash.toUrlString()
        val task = timerTasks[hash]

        if (task != null && task.interval != message.interval) {
            renewTask(task, message.interval)
        }

        context()!!
                .actorSelection("/user/tasks/task/${hash}")
                .forward(message, context()!!)
    }

    private fun renewTask(task: Task, interval: Long) {
        task.cancel()
        val newTask = task.withNewInterval(interval)
        timerTasks[newTask.torrentHash] = newTask
        timer.schedule(task, interval, interval)
    }

    override fun onReceive(message: Any?) {
        when (message) {
            is UpdateAnnounceMessage -> updateAnnounce(message)
            is TrackerInfoMessage -> updateTracker(message)
        }
    }
}