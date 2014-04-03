package ru.nobirds.torrent.kademlia.message.bencode

import java.io.OutputStream
import java.io.InputStream
import java.util.HashMap
import ru.nobirds.torrent.bencode.BMapHelper
import ru.nobirds.torrent.client.parser.Bencoder
import ru.nobirds.torrent.bencode.BTypeFactory
import ru.nobirds.torrent.kademlia.message.RequestMessage
import ru.nobirds.torrent.kademlia.message.RequestContainer
import ru.nobirds.torrent.kademlia.message.MessageSerializer
import ru.nobirds.torrent.kademlia.message.ResponseMessage
import ru.nobirds.torrent.kademlia.message.Message
import ru.nobirds.torrent.kademlia.message.MessageType
import ru.nobirds.torrent.kademlia.message.ErrorMessage
import ru.nobirds.torrent.kademlia.message.LostResponse
import java.net.InetSocketAddress

public class BencodeMessageSerializer(val requestContainer: RequestContainer) : MessageSerializer {

    private val requestClassesByCodes = HashMap<String, Class<out RequestMessage>>()

    private val codesByRequestClasses = HashMap<Class<out RequestMessage>, String>()

    private val marshallers = HashMap<Class<out RequestMessage>,
            BencodeMarshaller<out RequestMessage, out ResponseMessage>>()

    override fun deserialize(address:InetSocketAddress, source: InputStream): Message {
        val map =  BMapHelper(Bencoder.decodeBMap(source))

        val id = map.getString("t")!!
        val messageType = resolveMessageType(map.getString("y")!!)

        return when(messageType) {
            MessageType.error -> {
                val list = map.getList("e")!!
                ErrorMessage(id, address, list.getInt(0), list.getString(1))
            }
            MessageType.request -> {
                val requestType = map.getString("q")!!
                val marshaller = marshallers[resolveRequestTypeByCode(requestType)]!!

                marshaller.marshallRequest(id, address, map.getBMap("a")!!)
            }
            MessageType.response -> {
                val request = requestContainer.findById(id)
                if(request != null && request.mType == messageType) {

                    val marshaller = marshallers[request.javaClass]!!

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
        marshallers.put(requestType, marshaller)
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
                    val marshaller = marshallers[message.javaClass]!!
                        as BencodeMarshaller<RequestMessage, ResponseMessage>

                    map("a", marshaller.unmarshallRequest(message as RequestMessage))
                }
                MessageType.response -> {

                    val marshaller = marshallers[message.javaClass]!!
                        as BencodeMarshaller<RequestMessage, ResponseMessage>

                    map("r", marshaller.unmarshallResponse(message as ResponseMessage))
                }
            }
        }

        Bencoder.encodeBType(output, result)
    }

}