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

public class FindNodeBencodeMarshaller : BencodeMarshaller<FindNodeRequest, FindNodeResponse> {

    override fun marshallRequest(id:String, address:InetSocketAddress, map: BMap): FindNodeRequest {
        val sender = map.getBytes("id")!!
        val target = map.getBytes("target")!!

        return FindNodeRequest(id, Peer(Id.fromBytes(sender), address), Id.fromBytes(target))
    }

    override fun marshallResponse(id: String, address:InetSocketAddress, map: BMap): FindNodeResponse {
        val sender = map.getBytes("id")!!
        val nodes = map.getBytes("nodes")!!

        return FindNodeResponse(id, Peer(Id.fromBytes(sender), address), nodes.toInetSocketAddresses())
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