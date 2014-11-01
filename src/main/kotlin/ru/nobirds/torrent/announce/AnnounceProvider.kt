package ru.nobirds.torrent.announce

import org.springframework.web.client.RestTemplate
import java.net.URL
import org.springframework.web.util.UriComponentsBuilder
import ru.nobirds.torrent.client.task.TorrentTask
import ru.nobirds.torrent.peers.Peer
import org.springframework.util.MultiValueMap
import java.util.Collections
import java.net.InetSocketAddress
import java.util.HashMap
import java.util.Timer
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.parser.BEncodeHttpMessageConverter
import ru.nobirds.torrent.bencode.BType
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BBytes
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.net.InetAddress
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.nio.ByteOrder
import ru.nobirds.torrent.utils.toUrlString
import ru.nobirds.torrent.utils.multiValueMapOf
import ru.nobirds.torrent.utils.toInetSocketAddresses
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.toId
import java.net.URI

public trait AnnounceProvider {

    fun getTrackerInfoByUrl(uri:URI, localPeer: Peer, hash: Id): TrackerInfo

}

