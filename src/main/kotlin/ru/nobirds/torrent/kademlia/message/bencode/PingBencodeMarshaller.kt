package ru.nobirds.torrent.kademlia.message.bencode

import ru.nobirds.torrent.kademlia.message.PingRequest
import ru.nobirds.torrent.kademlia.message.PingResponse
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BMapHelper
import ru.nobirds.torrent.kademlia.Id
import ru.nobirds.torrent.bencode.BMapBuilder
import ru.nobirds.torrent.bencode.BTypeFactory

public class PingBencodeMarshaller : BencodeMarshaller<PingRequest, PingResponse> {

    override fun marshallRequest(id:Long, map: BMap): PingRequest {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!

        return PingRequest(id, Id.fromBytes(sender))
    }

    override fun marshallResponse(id: Long, map: BMap): PingResponse {
        val helper = BMapHelper(map)

        val sender = helper.getBytes("id")!!

        return PingResponse(id, Id.fromBytes(sender))
    }

    override fun unmarshallRequest(request: PingRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.toBytes())
    }

    override fun unmarshallResponse(response: PingResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.toBytes())
    }

}