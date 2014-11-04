package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.dht.message.FindNodeRequest
import ru.nobirds.torrent.dht.message.FindNodeResponse
import ru.nobirds.torrent.utils.toCompact
import java.net.InetSocketAddress
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.utils.toInetSocketAddresses
import ru.nobirds.torrent.utils.parse26BytesPeers

public class FindNodeBencodeMarshaller :
        RequestMarshaller<FindNodeRequest>, RequestUnmarshaller<FindNodeRequest>,
        ResponseMarshaller<FindNodeRequest, FindNodeResponse>, ResponseUnmarshaller<FindNodeResponse> {

    override fun marshallRequest(id:String, address:InetSocketAddress, map: BMap): FindNodeRequest {
        val sender = map.getBytes("id")!!
        val target = map.getBytes("target")!!

        return FindNodeRequest(id, Peer(Id.fromBytes(sender), address), Id.fromBytes(target))
    }

    override fun marshallResponse(address: InetSocketAddress, map: BMap, request: FindNodeRequest): FindNodeResponse {
        val sender = map.getBytes("id")!!
        val nodes = map.getBytes("nodes")!!

        return FindNodeResponse(Peer(Id.fromBytes(sender), address), request, nodes.parse26BytesPeers())
    }

    override fun unmarshallRequest(request: FindNodeRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.id.toBytes())
        value("target", request.target.toBytes())
    }

    override fun unmarshallResponse(response: FindNodeResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.id.toBytes())
        value("nodes", response.nodes.toCompact())
    }

}