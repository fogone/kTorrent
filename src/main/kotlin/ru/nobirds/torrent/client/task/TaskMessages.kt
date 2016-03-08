package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.connection.PeerAndMessage
import ru.nobirds.torrent.client.task.state.BlockPositionAndSize
import ru.nobirds.torrent.peers.Peer
import java.net.InetSocketAddress

interface TaskMessage

class HandleTaskMessage(val message:PeerAndMessage) : TaskMessage

class RehashTorrentFilesMessage() : TaskMessage
class AddPeersMessage(val peers:Set<InetSocketAddress>) : TaskMessage
class RequestBlockMessage(val peer: Peer, val positionAndSize: BlockPositionAndSize) : TaskMessage