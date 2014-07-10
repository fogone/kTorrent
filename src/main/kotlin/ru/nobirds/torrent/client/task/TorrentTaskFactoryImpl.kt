package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.model.Torrent
import java.nio.file.Path
import ru.nobirds.torrent.client.LocalPeerFactory
import ru.nobirds.torrent.client.DigestProvider

public class TorrentTaskFactoryImpl(val directory:Path, val portRange: LongRange, val digest: DigestProvider, val localPeerFactory: LocalPeerFactory) : TorrentTaskFactory {

    override fun create(torrent: Torrent): TorrentTask {
        return TorrentTask(localPeerFactory.createLocalPeer(portRange), directory, torrent, digest)
    }

}