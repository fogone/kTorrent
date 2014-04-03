package ru.nobirds.torrent.kademlia.message.bencode

import ru.nobirds.torrent.kademlia.message.PingRequest
import ru.nobirds.torrent.kademlia.message.PingResponse
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BMapHelper
import ru.nobirds.torrent.kademlia.Id
import ru.nobirds.torrent.bencode.BTypeFactory
import java.net.InetSocketAddress
import ru.nobirds.torrent.kademlia.Node

public class PingBencodeMarshaller : BencodeMarshaller<PingRequest, PingResponse> {

    override fun marshallRequest(id:String, address:InetSocketAddress, map: BMap): PingRequest {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!

        return PingRequest(id, Node(Id.fromBytes(sender), address))
    }

    override fun marshallResponse(id: String, address:InetSocketAddress, map: BMap): PingResponse {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!

        return PingResponse(id, Node(Id.fromBytes(sender), address))
    }

    override fun unmarshallRequest(request: PingRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.id.toBytes())
    }

    override fun unmarshallResponse(response: PingResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.id.toBytes())
    }

}