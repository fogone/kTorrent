package ru.nobirds.torrent.client

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.convert.converter.Converter
import ru.nobirds.torrent.client.connection.ConnectionManager
import ru.nobirds.torrent.client.connection.netty.NettyConnectionManager
import ru.nobirds.torrent.client.task.TaskManager
import ru.nobirds.torrent.dht.BootstrapHosts
import ru.nobirds.torrent.dht.Dht
import ru.nobirds.torrent.parser.TorrentParser
import ru.nobirds.torrent.parser.TorrentParserImpl
import ru.nobirds.torrent.peers.LocalPeerFactory
import ru.nobirds.torrent.peers.PeerManager
import ru.nobirds.torrent.peers.provider.DhtPeerProvider
import ru.nobirds.torrent.peers.provider.PeerProvider
import ru.nobirds.torrent.utils.availablePort
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import org.springframework.beans.factory.annotation.Autowired as autowired

@Configuration
@Import(ConfigConfiguration::class)
@EnableConfigurationProperties(ClientProperties::class)
open class TorrentClientConfiguration() {

    @Bean
    open fun torrentParser(): TorrentParser = TorrentParserImpl(sha1Provider())

    @Bean
    open fun connectionManager(localPeerFactory: LocalPeerFactory): ConnectionManager
            = NettyConnectionManager(localPeerFactory.port)

    @Bean
    open fun taskManager(config:ClientProperties, localPeerFactory: LocalPeerFactory, peerManager:PeerProvider, connectionManager: ConnectionManager): TaskManager
            = TaskManager(config.directory, peerManager, connectionManager, torrentParser(), sha1Provider())

    @Bean
    open fun localPeerFactory(config:ClientProperties): LocalPeerFactory = LocalPeerFactory(config.ports.availablePort())

    @Bean
    open fun peerManager(providers:List<PeerProvider>): PeerManager {
        val peerManager = PeerManager()
        for (provider in providers) {
            peerManager.registerProvider(provider)
        }
        return peerManager
    }

    @Bean
    open fun dht(config: ClientProperties): Dht = Dht(config.dhtPorts.availablePort(), BootstrapHosts.addresses.asSequence())

    @Bean
    open fun dhtPeerProvider(dht:Dht):PeerProvider = DhtPeerProvider(dht)

    @Bean
    open fun sha1Provider(): DigestProvider = DigestProvider { MessageDigest.getInstance("SHA-1") }

    @Bean
    open fun clientRunner(taskManager: TaskManager): ClientCommandLineRunner = ClientCommandLineRunner(taskManager)

}

@Configuration
open class ConfigConfiguration {

    @Bean
    open fun stringToPathConverter():Converter<String, Path>
            = Converter { source -> if(source == null) null else Paths.get(source) }

    @Bean
    open fun stringToIntRangeConverter():Converter<String, IntRange>
            = Converter { source ->
        if(source != null) {
            val (start, end) = source.split("\\.\\.")
            start.toInt().rangeTo(end.toInt())
        } else null
    }


}