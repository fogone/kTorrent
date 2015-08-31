package ru.nobirds.torrent.client

import org.springframework.beans.factory.config.CustomEditorConfigurer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.support.DefaultConversionService
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
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.client.connection.ConnectionManager
import ru.nobirds.torrent.client.connection.NettyConnectionManager
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
@Import(ConfigConfiguration::class)
@EnableConfigurationProperties(ClientProperties::class)
public open class TorrentClientConfiguration() {

    @Bean
    public open fun torrentParser(): TorrentParser = TorrentParserImpl(sha1Provider())

    @Bean
    public open fun executorService(config:ClientProperties): ExecutorService = Executors.newFixedThreadPool(config.threads)

    @Bean
    public open fun connectionManager(localPeer: Peer): ConnectionManager
            = NettyConnectionManager(localPeer.address.port)

    @Bean
    public open fun taskManager(config:ClientProperties, localPeer: Peer, peerManager:PeerProvider, connectionManager: ConnectionManager): TaskManager
            = TaskManager(config.directory, localPeer, peerManager, connectionManager, sha1Provider())

    @Bean
    public open fun localPeerFactory(config:ClientProperties): LocalPeerFactory
            = LocalPeerFactory(config.ports)

    @Bean
    public open fun peerManager(localPeer: Peer): PeerManager {
        val peerManager = PeerManager()
        peerManager.registerProvider(TrackerPeerProvider(localPeer))
        peerManager.registerProvider(DhtPeerProvider(localPeer))
        return peerManager
    }

    @Bean
    public open fun localPeer(localPeerFactory: LocalPeerFactory): Peer = localPeerFactory.createLocalPeer()

    @Bean
    public open fun sha1Provider(): DigestProvider = DigestProvider { MessageDigest.getInstance("SHA-1") }

    @Bean
    public open fun clientRunner(taskManage: TaskManager): ClientCommandLineRunner = ClientCommandLineRunner(taskManage)

}

@Configuration
public open class ConfigConfiguration {

/*
    @Bean
    public open fun conversionService(): ConversionService {
        val conversionService = DefaultConversionService()
        conversionService.addConverter()
        return conversionService
    }
*/

    @Bean
    public open fun stringToPathConverter():Converter<String, Path>
            = Converter { source -> if(source == null) null else Paths.get(source) }

    @Bean
    public open fun stringToLongRangeConverter():Converter<String, LongRange>
            = Converter { source ->
        if(source != null) {
            val (start, end) = source.split("\\.\\.")
            start.toLong().rangeTo(end.toLong())
        } else null
    }


}