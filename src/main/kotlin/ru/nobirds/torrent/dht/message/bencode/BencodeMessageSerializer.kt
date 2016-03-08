package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.dht.message.*
import ru.nobirds.torrent.utils.Id

class BencodeMessageSerializer(val localPeer: Id, val requestContainer: RequestContainer) : MessageSerializer {

    private val marshallers = MarshallerContainer()

    init {
        val pingMarshaller = PingBencodeMarshaller()
        val findNodeMarshaller = FindNodeBencodeMarshaller()
        val getPeersMarshaller = GetPeersBencodeMarshaller()
        val announcePeerMarshaller = AnnouncePeerBencodeMarshaller()

        marshallers.register(RequestType.ping, pingMarshaller, pingMarshaller, pingMarshaller, pingMarshaller)
        marshallers.register(RequestType.findNode, findNodeMarshaller, findNodeMarshaller, findNodeMarshaller, findNodeMarshaller)
        marshallers.register(RequestType.findPeer, getPeersMarshaller, getPeersMarshaller, getPeersMarshaller, getPeersMarshaller)
        marshallers.register(RequestType.announcePeer, announcePeerMarshaller, announcePeerMarshaller, announcePeerMarshaller, announcePeerMarshaller)
    }

    override fun deserialize(map: BMap): DhtMessage {
        val id = map.getString("t")!!

        val messageType = findMessageTypeByCode(map.getString("y")!!)

        return when(messageType) {
            MessageType.error -> {
                val requestMessage = requestContainer.removeById(id)
                val pair = map.get("e")!!

                if(pair.value is BList) {
                    val list = pair.value as BList
                    if (requestMessage != null) {
                        ErrorMessageResponse(requestMessage, list.getInt(0), list.getString(1), requestMessage.sender)
                    } else {
                        ErrorMessage(id, list.getInt(0), list.getString(1), Id.Zero)
                    }
                } else {
                    val bytes = pair.value as BBytes
                    if (requestMessage != null) {
                        ErrorMessageResponse(requestMessage, 0, bytes.toString(), requestMessage.sender)
                    } else {
                        ErrorMessage(id, 0, bytes.toString(), Id.Zero)
                    }
                }

            }
            MessageType.request -> {
                val queryType = map.getString("q")!!
                val requestType = findRequestTypeByCode(queryType)
                val marshaller = marshallers.findRequestMarshaller(requestType)

                marshaller.marshallRequest(id, map.getBMap("a")!!)
            }
            MessageType.response -> {
                val request = requestContainer.removeById(id)
                if(request != null) {
                    val marshaller = marshallers.findResponseMarshaller(request.type)
                    marshaller.marshallResponse(map.getBMap("r")!!, request)
                } else LostResponse(localPeer, LostRequest(localPeer))
            }
        }
    }

    override fun serialize(message: DhtMessage): BMap = BTypeFactory.createBMap {

        value("t", message.id)
        value("y", message.messageType.code)

        when(message.messageType) {
            MessageType.error -> {
                val error = message as ErrorMessage
                list("e") {
                    value(error.error)
                    value(error.message)
                }
            }
            MessageType.request -> {
                val requestMessage = message as RequestMessage

                val unmarshaller = marshallers.findRequestUnmarshaller(requestMessage.type)

                value("q", requestMessage.type.code)
                map("a", unmarshaller.unmarshallRequest(requestMessage))
            }
            MessageType.response -> {
                val responseMessage = message as ResponseMessage

                val marshaller = marshallers.findResponseUnmarshaller(responseMessage.request.type)

                map("r", marshaller.unmarshallResponse(responseMessage))
            }
        }
    }

}