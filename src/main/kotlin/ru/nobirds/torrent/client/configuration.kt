package ru.nobirds.torrent.client

import org.springframework.context.annotation.Configuration as configuration
import org.springframework.context.annotation.Bean as bean
import ru.nobirds.torrent.config.Config
import ru.nobirds.torrent.config.Configs
import org.springframework.beans.factory.annotation.Autowired as autowired
import ru.nobirds.torrent.parser.TorrentParser
import ru.nobirds.torrent.parser.TorrentParserImpl
import java.security.MessageDigest
import ru.nobirds.torrent.client.task.TaskManager
import ru.nobirds.torrent.peers.PeerManager
import ru.nobirds.torrent.peers.provider.TrackerPeerProvider
import ru.nobirds.torrent.peers.provider.DhtPeerProvider
import ru.nobirds.torrent.peers.provider.PeerProvider
import ru.nobirds.torrent.peers.LocalPeerFactory

public configuration open class TorrentClientConfiguration() {

    public bean open fun config(): Config = Configs.fromProperties("client.properties")

    public bean open fun torrentParser(): TorrentParser = TorrentParserImpl(sha1Provider())

    public bean open fun taskManager(
            config: Config, localPeerFactory: LocalPeerFactory, peerManager:PeerProvider): TaskManager =

            TaskManager(config.get(ClientProperties.torrentsDirectory),
                    localPeerFactory.createLocalPeer(),
                    peerManager,
                    sha1Provider())

    public bean open fun localPeerFactory(config: Config): LocalPeerFactory = LocalPeerFactory(
            config.get(ClientProperties.clientPortsRange)
    )

    public bean open fun peerManager(localPeerFactory: LocalPeerFactory): PeerManager {
        val peerManager = PeerManager()
        peerManager.registerProvider(TrackerPeerProvider(localPeerFactory.createLocalPeer()))
        peerManager.registerProvider(DhtPeerProvider(localPeerFactory.createLocalPeer()))
        return peerManager
    }

    public bean open fun sha1Provider(): DigestProvider = DigestProvider { MessageDigest.getInstance("SHA-1") }

}