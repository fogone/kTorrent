package ru.nobirds.torrent.dht.message.bencode

import ru.nobirds.torrent.bencode.BMap
import java.io.OutputStream
import java.io.InputStream
import java.util.HashMap
import ru.nobirds.torrent.parser.Bencoder
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.dht.message.RequestMessage
import ru.nobirds.torrent.dht.message.RequestContainer
import ru.nobirds.torrent.dht.message.MessageSerializer
import ru.nobirds.torrent.dht.message.ResponseMessage
import ru.nobirds.torrent.dht.message.Message
import ru.nobirds.torrent.dht.message.MessageType
import ru.nobirds.torrent.dht.message.ErrorMessage
import ru.nobirds.torrent.dht.message.LostResponse
import java.net.InetSocketAddress
import ru.nobirds.torrent.dht.message.PingRequest
import ru.nobirds.torrent.dht.message.FindNodeRequest
import ru.nobirds.torrent.dht.message.GetPeersRequest
import ru.nobirds.torrent.dht.message.AnnouncePeerRequest
import ru.nobirds.torrent.dht.message.PingResponse
import ru.nobirds.torrent.dht.message.FindNodeResponse
import ru.nobirds.torrent.dht.message.GetPeersResponse
import ru.nobirds.torrent.dht.message.AnnouncePeerResponse
import ru.nobirds.torrent.dht.message.LostRequest
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.dht.message.ErrorMessageResponse
import ru.nobirds.torrent.dht.message.BootstrapFindNodeRequest
import ru.nobirds.torrent.dht.message.RequestType
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.dht.message.findMessageTypeByCode
import ru.nobirds.torrent.dht.message.findRequestTypeByCode

public class BencodeMessageSerializer(val localPeer: Peer, val requestContainer: RequestContainer) : MessageSerializer {

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

    override fun deserialize(address: InetSocketAddress, map: BMap): Message {
        val id = map.getString("t")!!

        val messageType = findMessageTypeByCode(map.getString("y")!!)

        return when(messageType) {
            MessageType.error -> {
                val requestMessage = requestContainer.removeById(id)
                val list = map.getBList("e")!!
                if (requestMessage != null) {
                    ErrorMessageResponse(requestMessage, list.getInt(0), list.getString(1), localPeer)
                } else {
                    ErrorMessage(id, list.getInt(0), list.getString(1), Peer(Id.Zero, address))
                }
            }
            MessageType.request -> {
                val queryType = map.getString("q")!!
                val requestType = findRequestTypeByCode(queryType)
                val marshaller = marshallers.findRequestMarshaller(requestType)

                marshaller.marshallRequest(id, address, map.getBMap("a")!!)
            }
            MessageType.response -> {
                val request = requestContainer.removeById(id)
                if(request != null) {
                    val marshaller = marshallers.findResponseMarshaller(request.type)
                    marshaller.marshallResponse(address, map.getBMap("r")!!, request)
                } else LostResponse(localPeer, LostRequest(localPeer))
            }
        }
    }

    override fun serialize(message: Message): BMap = BTypeFactory.createBMap {

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