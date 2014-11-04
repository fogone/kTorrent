package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.dht.message.PingRequest
import ru.nobirds.torrent.dht.message.PingResponse
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.bencode.BTypeFactory
import java.net.InetSocketAddress
import ru.nobirds.torrent.peers.Peer

public class PingBencodeMarshaller :
        RequestMarshaller<PingRequest>, RequestUnmarshaller<PingRequest>,
        ResponseMarshaller<PingRequest, PingResponse>, ResponseUnmarshaller<PingResponse> {

    override fun marshallRequest(id:String, address:InetSocketAddress, map: BMap): PingRequest {
        val sender = map.getBytes("id")!!

        return PingRequest(id, Peer(Id.fromBytes(sender), address))
    }

    override fun marshallResponse(address: InetSocketAddress, map: BMap, request: PingRequest): PingResponse {
        val sender = map.getBytes("id")!!

        return PingResponse(Peer(Id.fromBytes(sender), address), request)
    }

    override fun unmarshallRequest(request: PingRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.id.toBytes())
    }

    override fun unmarshallResponse(response: PingResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.id.toBytes())
    }

}