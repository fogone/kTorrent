package ru.nobirds.torrent.kademlia.message.bencode

import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BMapHelper
import ru.nobirds.torrent.kademlia.Id
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.utils.toInetSocketAddresses
import ru.nobirds.torrent.utils.toCompact
import ru.nobirds.torrent.kademlia.message.GetPeersRequest
import ru.nobirds.torrent.kademlia.message.GetPeersResponse
import ru.nobirds.torrent.kademlia.message.ClosestPeersResponse
import ru.nobirds.torrent.kademlia.message.PeersFoundResponse
import java.net.InetSocketAddress
import ru.nobirds.torrent.bencode.BListHelper
import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.utils.toInetSocketAddress
import ru.nobirds.torrent.kademlia.Node

public class GetPeersBencodeMarshaller : BencodeMarshaller<GetPeersRequest, GetPeersResponse> {

    override fun marshallRequest(id:String, address:InetSocketAddress, map: BMap): GetPeersRequest {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!
        val hash = helper.getBytes("info_hash")!!

        return GetPeersRequest(id, Node(Id.fromBytes(sender), address), Id.fromBytes(hash))
    }

    override fun marshallResponse(id: String, address:InetSocketAddress, map: BMap): GetPeersResponse {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!
        val token = helper.getString("token")!!

        return if(helper.containsKey("nodes")) {
            val nodes = helper.getBytes("nodes")!!
            ClosestPeersResponse(id, Node(Id.fromBytes(sender), address), token, nodes.toInetSocketAddresses())
        } else {
            val values = helper.getList("values")!!
            PeersFoundResponse(id, Node(Id.fromBytes(sender), address), token, parseValues(values))
        }
    }

    private fun parseValues(list:BListHelper):List<InetSocketAddress>
            = list.map<BBytes, InetSocketAddress>() { it.value.toInetSocketAddress() }

    override fun unmarshallRequest(request: GetPeersRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.id.toBytes())
        value("info_hash", request.hash.toBytes())
    }

    override fun unmarshallResponse(response: GetPeersResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.id.toBytes())

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