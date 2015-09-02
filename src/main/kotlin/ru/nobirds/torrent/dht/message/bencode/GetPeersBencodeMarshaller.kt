package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.dht.Peer
import ru.nobirds.torrent.dht.message.ClosestNodesResponse
import ru.nobirds.torrent.dht.message.GetPeersRequest
import ru.nobirds.torrent.dht.message.GetPeersResponse
import ru.nobirds.torrent.dht.message.PeersFoundResponse
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.parse26BytesPeers
import ru.nobirds.torrent.utils.toCompact
import ru.nobirds.torrent.utils.toInetSocketAddress
import java.net.InetSocketAddress
import java.util.*

public class GetPeersBencodeMarshaller :
        RequestMarshaller<GetPeersRequest>, RequestUnmarshaller<GetPeersRequest>,
        ResponseMarshaller<GetPeersRequest, GetPeersResponse>, ResponseUnmarshaller<GetPeersResponse> {

    override fun marshallRequest(id:String, map: BMap): GetPeersRequest {
        val sender = map.getBytes("id")!!
        val hash = map.getBytes("info_hash")!!

        return GetPeersRequest(id, Id.fromBytes(sender), Id.fromBytes(hash))
    }

    override fun marshallResponse(map: BMap, request: GetPeersRequest): GetPeersResponse {
        val sender = map.getBytes("id")!!
        val token = map.getString("token")

        return if(map.containsKey("values")) {
            PeersFoundResponse(Id.fromBytes(sender), request,  token, parseValues(map.getBList("values")!!))
        } else {
            val nodes = map.getBytes("nodes") ?: map.getBytes("value")
            ClosestNodesResponse(Id.fromBytes(sender), request, token,
                    if(nodes!=null) nodes.parse26BytesPeers().map { Peer(it.first, it.second) } else Collections.emptyList())
        }
    }

    private fun parseValues(list:BList):List<InetSocketAddress>
            = list.map { (it as BBytes).value.toInetSocketAddress() }

    override fun unmarshallRequest(request: GetPeersRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.toBytes())
        value("info_hash", request.hash.toBytes())
    }

    override fun unmarshallResponse(response: GetPeersResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.toBytes())

        when (response) {
            is ClosestNodesResponse -> {
                value("nodes", response.nodes.map { Pair(it.id, it.address) }.toCompact())
            }
            is PeersFoundResponse -> {
                list("values") {
                    for (addr in response.nodes) {
                        value(addr.toCompact())
                    }
                }
            }
        }
    }

}