package ru.nobirds.torrent.kademlia.message.bencode

import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BMapHelper
import ru.nobirds.torrent.kademlia.Id
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.kademlia.message.FindNodeRequest
import ru.nobirds.torrent.kademlia.message.FindNodeResponse
import ru.nobirds.torrent.utils.toInetSocketAddresses
import ru.nobirds.torrent.utils.toCompact
import java.net.InetSocketAddress
import ru.nobirds.torrent.kademlia.Node

public class FindNodeBencodeMarshaller : BencodeMarshaller<FindNodeRequest, FindNodeResponse> {

    override fun marshallRequest(id:String, address:InetSocketAddress, map: BMap): FindNodeRequest {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!
        val target = helper.getBytes("target")!!

        return FindNodeRequest(id, Node(Id.fromBytes(sender), address), Id.fromBytes(target))
    }

    override fun marshallResponse(id: String, address:InetSocketAddress, map: BMap): FindNodeResponse {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!
        val nodes = helper.getBytes("nodes")!!

        return FindNodeResponse(id, Node(Id.fromBytes(sender), address), nodes.toInetSocketAddresses())
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