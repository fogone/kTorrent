package ru.nobirds.torrent.client.task


import ru.nobirds.torrent.utils.actorOf
import akka.actor.UntypedActor
import akka.event.Logging
import ru.nobirds.torrent.utils.toHexString

public class TorrentTaskManagerActor(val torrentTaskFactory: TorrentTaskFactory) : UntypedActor() {

    private val log = Logging.getLogger(context()!!.system()!!, this);

    override fun onReceive(message: Any?) {
        when(message) {
            is AddTorrentMessage -> getContext()!!
                    .actorOf("task/" + message.torrent.info.hash!!.toHexString()) { torrentTaskFactory.create(message.torrent) }
        }
    }

}