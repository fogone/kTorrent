package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.connection.PeerAndMessage
import ru.nobirds.torrent.client.task.state.BlockPositionAndSize
import ru.nobirds.torrent.peers.Peer
import java.net.InetSocketAddress

public interface TaskMessage

public class HandleTaskMessage(val message:PeerAndMessage) : TaskMessage

public class RehashTorrentFilesMessage() : TaskMessage
public class AddPeersMessage(val peers:Set<InetSocketAddress>) : TaskMessage
public class RequestBlockMessage(val peer: Peer, val positionAndSize: BlockPositionAndSize) : TaskMessage