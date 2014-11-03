package ru.nobirds.torrent.dht.message.bencode

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

class MarshallerContainer() {

    private val marshallers = HashMap<Class<out Any>,
            BencodeMarshaller<RequestMessage, ResponseMessage>>()

    public fun register(type:Class<out Any>, marshaller:BencodeMarshaller<out RequestMessage, out ResponseMessage>) {
        marshallers.put(type, marshaller as BencodeMarshaller<RequestMessage, ResponseMessage>)
    }

    public fun get(type:Class<out Any>): BencodeMarshaller<RequestMessage, ResponseMessage> {
        return marshallers[type]
    }
}

public class BencodeMessageSerializer(val requestContainer: RequestContainer) : MessageSerializer {

    private val requestClassesByCodes = HashMap<String, Class<out RequestMessage>>()

    private val codesByRequestClasses = HashMap<Class<out RequestMessage>, String>()

    private val marshallers = MarshallerContainer()

    ;{
        registerMarshaller("ping", javaClass<PingRequest>(), PingBencodeMarshaller())
        registerMarshaller("find_node", javaClass<FindNodeRequest>(), FindNodeBencodeMarshaller())
        registerMarshaller("get_peers", javaClass<GetPeersRequest>(), GetPeersBencodeMarshaller())
        registerMarshaller("announce_peer", javaClass<AnnouncePeerRequest>(), AnnouncePeerBencodeMarshaller())
    }

    override fun deserialize(address:InetSocketAddress, source: InputStream): Message {
        val map =  Bencoder.decodeBMap(source)

        val id = map.getString("t")!!
        val messageType = resolveMessageType(map.getString("y")!!)

        return when(messageType) {
            MessageType.error -> {
                val list = map.getBList("e")!!
                ErrorMessage(id, address, list.getInt(0), list.getString(1))
            }
            MessageType.request -> {
                val requestType = map.getString("q")!!
                val marshaller = marshallers[resolveRequestTypeByCode(requestType)]

                marshaller.marshallRequest(id, address, map.getBMap("a")!!)
            }
            MessageType.response -> {
                val request = requestContainer.findById(id)
                if(request != null && request.mType == messageType) {

                    val marshaller = marshallers[request.javaClass]

                    marshaller.marshallResponse(id, address, map.getBMap("r")!!)
                } else LostResponse(address)
            }
        }
    }

    private fun resolveRequestTypeByCode(code:String):Class<out RequestMessage> = requestClassesByCodes[code]!!

    private fun resolveMessageType(code:String): MessageType = MessageType.values().first { it.code == code }

    public fun registerMarshaller<RQ: RequestMessage, RS:ResponseMessage>(
            code:String, requestType:Class<RQ>, marshaller: BencodeMarshaller<RQ, RS>) {

        requestClassesByCodes.put(code, requestType)
        codesByRequestClasses.put(requestType, code)
        marshallers.register(requestType, marshaller)
    }

    override fun serialize(message: Message, output: OutputStream) {
        val result = BTypeFactory.createBMap {

            value("t", message.id)
            value("y", message.mType.code)

            when(message.mType) {
                MessageType.error -> {
                    val error = message as ErrorMessage
                    list("e") {
                        value(error.error)
                        value(error.message)
                    }
                }
                MessageType.request -> {
                    val marshaller = marshallers[message.javaClass]

                    map("a", marshaller.unmarshallRequest(message as RequestMessage))
                }
                MessageType.response -> {
                    val marshaller = marshallers[message.javaClass]

                    map("r", marshaller.unmarshallResponse(message as ResponseMessage))
                }
            }
        }

        Bencoder.encodeBType(output, result)
    }

}