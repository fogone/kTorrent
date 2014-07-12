package ru.nobirds.torrent.client

import org.springframework.context.annotation.Configuration as configuration
import org.springframework.context.annotation.Bean as bean
import ru.nobirds.torrent.config.Config
import ru.nobirds.torrent.config.Configs
import org.springframework.beans.factory.annotation.Autowired as autowired
import ru.nobirds.torrent.client.parser.TorrentParser
import ru.nobirds.torrent.client.parser.TorrentParserImpl
import java.security.MessageDigest
import ru.nobirds.torrent.client.task.TorrentTaskFactory
import ru.nobirds.torrent.client.task.TorrentTaskFactoryImpl
import akka.actor.ActorSystem
import com.typesafe.config.Config as ActorConfig
import com.typesafe.config.ConfigFactory
import akka.actor.ActorRef
import ru.nobirds.torrent.client.task.TorrentTaskManagerActor
import ru.nobirds.torrent.utils.actorOf
import ru.nobirds.torrent.client.announce.UpdateAnnounceActor
import ru.nobirds.torrent.client.announce.TrackersActor

public configuration open class TorrentClientConfiguration() {

    public bean open fun actorSystemConfiguration():ActorConfig = ConfigFactory.defaultReference()!!

    public bean open fun actorSystem():ActorSystem = ActorSystem.create("ktorrent", actorSystemConfiguration())!!

    public bean open fun config(): Config = Configs.fromProperties("client.properties")

    /* actors */
    public bean open fun torrentTaskManager(): ActorRef = actorSystem()
            .actorOf("tasks") { TorrentTaskManagerActor(torrentTaskFactory()) }

    public bean open fun announces(): ActorRef = actorSystem()
            .actorOf("announces") { UpdateAnnounceActor() }

    public bean open fun trackers(): ActorRef = actorSystem()
            .actorOf("trakers") { TrackersActor() }

    /* beans */
    public bean open fun torrentTaskFactory(): TorrentTaskFactory =
            TorrentTaskFactoryImpl(
                    config()[ClientProperties.torrentsDirectory],
                    config()[ClientProperties.clientPortsRange],
                    sha1Provider(), localPeerFactory()
            )

    public bean open fun torrentParser(): TorrentParser = TorrentParserImpl(sha1Provider())

    public bean open fun localPeerFactory(): LocalPeerFactory = LocalPeerFactory(sha1Provider())

    public bean open fun sha1Provider(): DigestProvider = DigestProvider { MessageDigest.getInstance("SHA-1") }

}