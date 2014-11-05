package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.utils.toCompact
import ru.nobirds.torrent.dht.message.GetPeersRequest
import ru.nobirds.torrent.dht.message.GetPeersResponse
import ru.nobirds.torrent.dht.message.ClosestNodesResponse
import ru.nobirds.torrent.dht.message.PeersFoundResponse
import java.net.InetSocketAddress
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.utils.toInetSocketAddress
import ru.nobirds.torrent.utils.parse26BytesPeers
import java.util.Collections

public class GetPeersBencodeMarshaller :
        RequestMarshaller<GetPeersRequest>, RequestUnmarshaller<GetPeersRequest>,
        ResponseMarshaller<GetPeersRequest, GetPeersResponse>, ResponseUnmarshaller<GetPeersResponse> {

    override fun marshallRequest(id:String, address:InetSocketAddress, map: BMap): GetPeersRequest {
        val sender = map.getBytes("id")!!
        val hash = map.getBytes("info_hash")!!

        return GetPeersRequest(id, Peer(Id.fromBytes(sender), address), Id.fromBytes(hash))
    }

    override fun marshallResponse(address: InetSocketAddress, map: BMap, request: GetPeersRequest): GetPeersResponse {
        val sender = map.getBytes("id")!!
        val token = map.getString("token")

        return if(map.containsKey("values")) {
            PeersFoundResponse(Peer(Id.fromBytes(sender), address), request,  token, parseValues(map.getBList("values")!!))
        } else {
            val nodes = map.getBytes("nodes") ?: map.getBytes("value")
            ClosestNodesResponse(Peer(Id.fromBytes(sender), address), request, token, if(nodes!=null) nodes.parse26BytesPeers() else Collections.emptyList())
        }
    }

    private fun parseValues(list:BList):List<InetSocketAddress>
            = list.map { (it as BBytes).value.toInetSocketAddress() }

    override fun unmarshallRequest(request: GetPeersRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.id.toBytes())
        value("info_hash", request.hash.toBytes())
    }

    override fun unmarshallResponse(response: GetPeersResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.id.toBytes())

        when (response) {
            is ClosestNodesResponse -> {
                value("nodes", response.nodes.toCompact())
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