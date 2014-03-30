package ru.nobirds.torrent.kademlia.message.bencode

import ru.nobirds.torrent.kademlia.message.PingRequest
import ru.nobirds.torrent.kademlia.message.PingResponse
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BMapHelper
import ru.nobirds.torrent.kademlia.Id
import ru.nobirds.torrent.bencode.BMapBuilder
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.kademlia.message.FindNodeRequest
import ru.nobirds.torrent.kademlia.message.FindNodeResponse
import ru.nobirds.torrent.kademlia.Node
import ru.nobirds.torrent.utils.toInetSocketAddresses
import ru.nobirds.torrent.utils.toCompact
import ru.nobirds.torrent.kademlia.message.GetPeersRequest
import ru.nobirds.torrent.kademlia.message.GetPeersResponse
import ru.nobirds.torrent.kademlia.message.ClosestPeersResponse
import ru.nobirds.torrent.kademlia.message.PeersFoundResponse
import java.net.InetSocketAddress
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BListHelper
import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.utils.toInetSocketAddress
import ru.nobirds.torrent.utils.nullOr
import ru.nobirds.torrent.kademlia.message.AnnouncePeerRequest
import ru.nobirds.torrent.kademlia.message.AnnouncePeerResponse

public class AnnouncePeerBencodeMarshaller : BencodeMarshaller<AnnouncePeerRequest, AnnouncePeerResponse> {

    override fun marshallRequest(id:Long, map: BMap): AnnouncePeerRequest {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!
        val impliedPortValue = helper.getString("implied_port")
        val impliedPort = impliedPortValue != null && impliedPortValue == "1"

        val hash = helper.getBytes("info_hash")!!
        val port = helper.getInt("port")

        val token = helper.getString("token")!!

        return AnnouncePeerRequest(id, Id.fromBytes(sender), Id.fromBytes(hash), impliedPort, port, token)
    }

    override fun marshallResponse(id: Long, map: BMap): AnnouncePeerResponse {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!

        return AnnouncePeerResponse(id, Id.fromBytes(sender))
    }

    private fun parseValues(list:BListHelper):List<InetSocketAddress>
            = list.map<BBytes, InetSocketAddress>() { it.value.toInetSocketAddress() }

    override fun unmarshallRequest(request: AnnouncePeerRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.toBytes())
        value("info_hash", request.hash.toBytes())

        if(request.impliedPort)
            value("implied_port", "1")

        if(request.port != null)
            value("port", request.port.toLong())

        value("token", request.token)
    }

    override fun unmarshallResponse(response: AnnouncePeerResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.toBytes())
    }

}