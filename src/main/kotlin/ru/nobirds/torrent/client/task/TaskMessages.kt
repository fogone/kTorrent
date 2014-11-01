package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.peers.Peer
import java.net.Socket

public trait TaskMessage

public class RehashTorrentFilesMessage() : TaskMessage