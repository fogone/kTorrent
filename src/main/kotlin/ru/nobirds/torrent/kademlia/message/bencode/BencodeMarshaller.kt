package ru.nobirds.torrent.kademlia.message.bencode

import ru.nobirds.torrent.kademlia.message.RequestMessage
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.kademlia.message.ResponseMessage
import ru.nobirds.torrent.kademlia.message.PingRequest
import ru.nobirds.torrent.kademlia.message.ErrorMessage

public trait BencodeMarshaller<RQ: RequestMessage, RS:ResponseMessage> {

    fun marshallRequest(id:Long, map:BMap): RQ

    fun marshallResponse(id:Long, map:BMap): RS

    fun unmarshallRequest(request: RQ):BMap

    fun unmarshallResponse(response: RS):BMap

}

