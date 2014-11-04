package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.dht.message.RequestMessage
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.dht.message.ResponseMessage
import java.net.InetSocketAddress

public trait RequestMarshaller<RM: RequestMessage> {

    fun marshallRequest(id:String, address:InetSocketAddress, map:BMap): RM

}

public trait ResponseMarshaller<RqM: RequestMessage, RsM: ResponseMessage> {

    fun marshallResponse(address: InetSocketAddress, map: BMap, request: RqM): RsM

}

public trait RequestUnmarshaller<RM: RequestMessage> {

    fun unmarshallRequest(request: RM):BMap

}


public trait ResponseUnmarshaller<RM: ResponseMessage> {

    fun unmarshallResponse(response: RM):BMap

}

