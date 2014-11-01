package ru.nobirds.torrent.kademlia.message.bencode

import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.utils.toCompact
import ru.nobirds.torrent.kademlia.message.GetPeersRequest
import ru.nobirds.torrent.kademlia.message.GetPeersResponse
import ru.nobirds.torrent.kademlia.message.ClosestPeersResponse
import ru.nobirds.torrent.kademlia.message.PeersFoundResponse
import java.net.InetSocketAddress
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.toInetSocketAddresses
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.utils.toInetSocketAddress

public class GetPeersBencodeMarshaller : BencodeMarshaller<GetPeersRequest, GetPeersResponse> {

    override fun marshallRequest(id:String, address:InetSocketAddress, map: BMap): GetPeersRequest {
        val sender = map.getBytes("id")!!
        val hash = map.getBytes("info_hash")!!

        return GetPeersRequest(id, Peer(Id.fromBytes(sender), address), Id.fromBytes(hash))
    }

    override fun marshallResponse(id: String, address:InetSocketAddress, map: BMap): GetPeersResponse {
        val sender = map.getBytes("id")!!
        val token = map.getString("token")!!

        return if(map.containsKey("nodes")) {
            val nodes = map.getBytes("nodes")!!
            ClosestPeersResponse(id, Peer(Id.fromBytes(sender), address), token, nodes.toInetSocketAddresses())
        } else {
            val values = map.getBList("values")!!
            PeersFoundResponse(id, Peer(Id.fromBytes(sender), address), token, parseValues(values))
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