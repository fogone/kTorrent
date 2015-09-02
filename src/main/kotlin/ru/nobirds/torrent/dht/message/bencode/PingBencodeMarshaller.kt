package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.dht.message.PingRequest
import ru.nobirds.torrent.dht.message.PingResponse
import ru.nobirds.torrent.utils.Id

public class PingBencodeMarshaller :
        RequestMarshaller<PingRequest>, RequestUnmarshaller<PingRequest>,
        ResponseMarshaller<PingRequest, PingResponse>, ResponseUnmarshaller<PingResponse> {

    override fun marshallRequest(id:String, map: BMap): PingRequest {
        val sender = map.getBytes("id")!!

        return PingRequest(id, Id.fromBytes(sender))
    }

    override fun marshallResponse(map: BMap, request: PingRequest): PingResponse {
        val sender = map.getBytes("id")!!

        return PingResponse(Id.fromBytes(sender), request)
    }

    override fun unmarshallRequest(request: PingRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.toBytes())
    }

    override fun unmarshallResponse(response: PingResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.toBytes())
    }

}