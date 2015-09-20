package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.dht.message.RequestMessage
import ru.nobirds.torrent.dht.message.RequestType
import ru.nobirds.torrent.dht.message.ResponseMessage
import java.util.*

class MarshallerContainer() {

    private val requestMarshallers = HashMap<RequestType, RequestMarshaller<RequestMessage>>()
    private val responseMarshallers = HashMap<RequestType, ResponseMarshaller<RequestMessage, ResponseMessage>>()

    private val requestUnmarshallers = HashMap<RequestType, RequestUnmarshaller<RequestMessage>>()
    private val responseUnmarshallers = HashMap<RequestType, ResponseUnmarshaller<ResponseMessage>>()

    fun <R: RequestMessage> registerRequestMarshaller(requestType: RequestType, marshaller: RequestMarshaller<R>) {
        requestMarshallers.put(requestType, marshaller)
    }

    fun <RqM: RequestMessage, RsM: ResponseMessage> registerResponseMarshaller(requestType: RequestType, marshaller: ResponseMarshaller<RqM, RsM>) {
        responseMarshallers.put(requestType, marshaller as ResponseMarshaller<RequestMessage, ResponseMessage>)
    }

    fun <R : RequestMessage> registerRequestUnmarshaller(requestType: RequestType, unmarshaller: RequestUnmarshaller<R>) {
        requestUnmarshallers.put(requestType, unmarshaller as RequestUnmarshaller<RequestMessage>)
    }

    fun <R : ResponseMessage> registerResponseUnmarshaller(requestType: RequestType, unmarshaller: ResponseUnmarshaller<R>) {
        responseUnmarshallers.put(requestType, unmarshaller as ResponseUnmarshaller<ResponseMessage>)
    }

    fun <RqM : RequestMessage, RsM : ResponseMessage> register(requestType: RequestType,
                                                               requestMarshaller: RequestMarshaller<RqM>,
                                                               responseMarshaller: ResponseMarshaller<RqM, RsM>,
                                                               requestUnmarshaller: RequestUnmarshaller<RqM>,
                                                               responseUnmarshaller: ResponseUnmarshaller<RsM>) {
        registerRequestMarshaller(requestType, requestMarshaller)
        registerResponseMarshaller(requestType, responseMarshaller)
        registerRequestUnmarshaller(requestType, requestUnmarshaller)
        registerResponseUnmarshaller(requestType, responseUnmarshaller)
    }

    fun findRequestMarshaller(requestType: RequestType): RequestMarshaller<RequestMessage>
            = requestMarshallers.get(requestType)!!

    fun findResponseMarshaller(requestType: RequestType): ResponseMarshaller<RequestMessage, ResponseMessage>
            = responseMarshallers.get(requestType)!!

    fun findRequestUnmarshaller(requestType: RequestType): RequestUnmarshaller<RequestMessage>
            = requestUnmarshallers.get(requestType)!!

    fun findResponseUnmarshaller(requestType: RequestType): ResponseUnmarshaller<ResponseMessage>
            = responseUnmarshallers.get(requestType)!!

}