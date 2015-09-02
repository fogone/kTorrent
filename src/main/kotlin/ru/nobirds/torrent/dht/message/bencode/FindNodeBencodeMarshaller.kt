package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.dht.Peer
import ru.nobirds.torrent.dht.message.FindNodeRequest
import ru.nobirds.torrent.dht.message.FindNodeResponse
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.parse26BytesPeers
import ru.nobirds.torrent.utils.toCompact

public class FindNodeBencodeMarshaller :
        RequestMarshaller<FindNodeRequest>, RequestUnmarshaller<FindNodeRequest>,
        ResponseMarshaller<FindNodeRequest, FindNodeResponse>, ResponseUnmarshaller<FindNodeResponse> {

    override fun marshallRequest(id:String, map: BMap): FindNodeRequest {
        val sender = map.getBytes("id")!!
        val target = map.getBytes("target")!!

        return FindNodeRequest(id, Id.fromBytes(sender), Id.fromBytes(target))
    }

    override fun marshallResponse(map: BMap, request: FindNodeRequest): FindNodeResponse {
        val sender = map.getBytes("id")!!
        val nodes = map.getBytes("nodes")!!

        return FindNodeResponse(Id.fromBytes(sender), request, nodes.parse26BytesPeers().map { Peer(it.first, it.second) })
    }

    override fun unmarshallRequest(request: FindNodeRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.toBytes())
        value("target", request.target.toBytes())
    }

    override fun unmarshallResponse(response: FindNodeResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.toBytes())
        value("nodes", response.nodes.map { Pair(it.id, it.address) }.toCompact())
    }

}