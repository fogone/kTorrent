package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.dht.message.RequestType
import ru.nobirds.torrent.dht.message.RequestMessage
import ru.nobirds.torrent.dht.message.ResponseMessage
import java.util.HashMap

class MarshallerContainer() {

    private val requestMarshallers = HashMap<RequestType, RequestMarshaller<out RequestMessage>>()
    private val responseMarshallers = HashMap<RequestType, ResponseMarshaller<out RequestMessage, out ResponseMessage>>()

    private val requestUnmarshallers = HashMap<RequestType, RequestUnmarshaller<out RequestMessage>>()
    private val responseUnmarshallers = HashMap<RequestType, ResponseUnmarshaller<out ResponseMessage>>()

    fun <R: RequestMessage> registerRequestMarshaller(requestType: RequestType, marshaller: RequestMarshaller<R>) {
        requestMarshallers.put(requestType, marshaller)
    }

    fun <RqM: RequestMessage, RsM: ResponseMessage> registerResponseMarshaller(requestType: RequestType, marshaller: ResponseMarshaller<RqM, RsM>) {
        responseMarshallers.put(requestType, marshaller)
    }

    fun <R : RequestMessage> registerRequestUnmarshaller(requestType: RequestType, unmarshaller: RequestUnmarshaller<R>) {
        requestUnmarshallers.put(requestType, unmarshaller)
    }

    fun <R : ResponseMessage> registerResponseUnmarshaller(requestType: RequestType, unmarshaller: ResponseUnmarshaller<R>) {
        responseUnmarshallers.put(requestType, unmarshaller)
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
            = requestMarshallers.get(requestType) as RequestMarshaller<RequestMessage>

    fun findResponseMarshaller(requestType: RequestType): ResponseMarshaller<RequestMessage, ResponseMessage>
            = responseMarshallers.get(requestType) as ResponseMarshaller<RequestMessage, ResponseMessage>

    fun findRequestUnmarshaller(requestType: RequestType): RequestUnmarshaller<RequestMessage>
            = requestUnmarshallers.get(requestType) as RequestUnmarshaller<RequestMessage>

    fun findResponseUnmarshaller(requestType: RequestType): ResponseUnmarshaller<ResponseMessage>
            = responseUnmarshallers.get(requestType) as ResponseUnmarshaller<ResponseMessage>

}