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

public class GetPeersBencodeMarshaller : BencodeMarshaller<GetPeersRequest, GetPeersResponse> {

    override fun marshallRequest(id:Long, map: BMap): GetPeersRequest {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!
        val hash = helper.getBytes("info_hash")!!

        return GetPeersRequest(id, Id.fromBytes(sender), Id.fromBytes(hash))
    }

    override fun marshallResponse(id: Long, map: BMap): GetPeersResponse {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!
        val token = helper.getString("token")!!

        return if(helper.hasKey("nodes")) {
            val nodes = helper.getBytes("nodes")!!
            ClosestPeersResponse(id, Id.fromBytes(sender), token, nodes.toInetSocketAddresses())
        } else {
            val values = helper.getList("values")!!
            PeersFoundResponse(id, Id.fromBytes(sender), token, parseValues(values))
        }
    }

    private fun parseValues(list:BListHelper):List<InetSocketAddress> {

    }

    override fun unmarshallRequest(request: GetPeersRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.toBytes())
        value("info_hash", request.hash.toBytes())
    }

    override fun unmarshallResponse(response: GetPeersResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.toBytes())

        if(response is ClosestPeersResponse)
            value("nodes", response.nodes.toCompact())
        else
            list("values") {
                for (addr in response.nodes) {
                    value(addr.toCompact())
                }
            }
    }

}