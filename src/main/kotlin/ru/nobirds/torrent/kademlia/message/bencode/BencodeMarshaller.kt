package ru.nobirds.torrent.kademlia.message.bencode

import ru.nobirds.torrent.kademlia.message.RequestMessage
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.kademlia.message.ResponseMessage
import java.net.InetSocketAddress

public trait BencodeMarshaller<RQ: RequestMessage, RS:ResponseMessage> {

    fun marshallRequest(id:String, address:InetSocketAddress, map:BMap): RQ

    fun marshallResponse(id:String, address:InetSocketAddress, map:BMap): RS

    fun unmarshallRequest(request: RQ):BMap

    fun unmarshallResponse(response: RS):BMap

}

