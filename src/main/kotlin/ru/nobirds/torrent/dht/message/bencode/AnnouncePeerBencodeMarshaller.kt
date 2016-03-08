package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.dht.message.AnnouncePeerRequest
import ru.nobirds.torrent.dht.message.AnnouncePeerResponse
import ru.nobirds.torrent.utils.Id

class AnnouncePeerBencodeMarshaller() :
        RequestMarshaller<AnnouncePeerRequest>, RequestUnmarshaller<AnnouncePeerRequest>,
        ResponseMarshaller<AnnouncePeerRequest, AnnouncePeerResponse>, ResponseUnmarshaller<AnnouncePeerResponse> {

    override fun marshallRequest(id: String, map: BMap): AnnouncePeerRequest {
        val sender = map.getBytes("id")!!
        val impliedPortValue = map.getString("implied_port")
        val impliedPort = impliedPortValue != null && impliedPortValue == "1"

        val hash = map.getBytes("info_hash")!!
        val port = map.getInt("port")

        val token = map.getString("token")!!

        return AnnouncePeerRequest(id, Id.fromBytes(sender), Id.fromBytes(hash), impliedPort, port, token)
    }

    override fun unmarshallRequest(request: AnnouncePeerRequest): BMap = BTypeFactory.createBMap {
        value("id", request.sender.toBytes())
        value("info_hash", request.hash.toBytes())

        if(request.impliedPort)
            value("implied_port", "1")

        if(request.port != null)
            value("port", request.port.toLong())

        value("token", request.token)
    }


    override fun marshallResponse(map: BMap, request: AnnouncePeerRequest): AnnouncePeerResponse {
        val sender = map.getBytes("id")!!

        return AnnouncePeerResponse(Id.fromBytes(sender), request)
    }

    override fun unmarshallResponse(response: AnnouncePeerResponse): BMap = BTypeFactory.createBMap {
        value("id", response.sender.toBytes())
    }

}


