package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.Peer
import java.net.Socket

public trait TaskMessage

public class AddConnectionMessage(val socket:Socket) : TaskMessage
public class RemoveConnectionMessage(val connection:Connection) : TaskMessage
public class UpdatePeersMessage(val peers:List<Peer>) : TaskMessage
public class RehashTorrentFilesMessage() : TaskMessage
public class InitializeTrackersMessage() : TaskMessage
