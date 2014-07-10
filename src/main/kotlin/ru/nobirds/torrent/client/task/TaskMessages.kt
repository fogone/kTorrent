package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.Peer
import java.net.URL
import ru.nobirds.torrent.client.model.Torrent

public data class RemoveConnectionMessage(val peer: Peer)
public data class UpdatePeersMessage(val peers:List<Peer>)
public data class RehashTorrentFilesMessage()
public data class InitializeTrackersMessage()

public data class AddTorrentMessage(val torrent: Torrent)