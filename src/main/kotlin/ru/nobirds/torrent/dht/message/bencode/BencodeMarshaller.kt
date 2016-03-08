package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.dht.message.RequestMessage
import ru.nobirds.torrent.dht.message.ResponseMessage

interface RequestMarshaller<out RM: RequestMessage> {

    fun marshallRequest(id:String, map:BMap): RM

}

interface ResponseMarshaller<in RqM: RequestMessage, out RsM: ResponseMessage> {

    fun marshallResponse(map: BMap, request: RqM): RsM

}

interface RequestUnmarshaller<in RM: RequestMessage> {

    fun unmarshallRequest(request: RM):BMap

}


interface ResponseUnmarshaller<in RM: ResponseMessage> {

    fun unmarshallResponse(response: RM):BMap

}


