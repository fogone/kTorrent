package ru.nobirds.torrent.kademlia.message

import java.io.OutputStream
import java.io.InputStream
import java.util.HashMap
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BMapHelper
import ru.nobirds.torrent.client.parser.Bencoder
import ru.nobirds.torrent.bencode.BTypeFactory

public trait BencodeRequestMarshaller<RQ:RequestMessage> {

    fun marshall(map:BMap):RQ

    fun unmarshall(request:RQ):BMap

}

public trait BencodeResponseMarshaller<RQ:RequestMessage, RS:ResponseMessage<RQ>> {

    fun marshall(request:RQ, map:BMap):RS

    fun unmarshall(request:RS):BMap

}

data class BencodeMarshaller<RQ:RequestMessage, RS:ResponseMessage<RQ>>(
        val code:String,
        val requestClass:Class<RQ>,
        val request:BencodeRequestMarshaller<RQ>,
        val response:BencodeResponseMarshaller<RQ, RS>
)

public class BencodeMessageSerializer(val requestContainer: RequestContainer) : MessageSerializer {

    private val requestClassesByCodes = HashMap<String, Class<out RequestMessage>>()
    private val codesByRequestClasses = HashMap<Class<out RequestMessage>, String>()
    private val marshallers = HashMap<Class<out RequestMessage>, BencodeMarshaller<out RequestMessage, out ResponseMessage<out RequestMessage>>>()

    override fun serialize(source: InputStream): Message {
        val map =  BMapHelper(Bencoder.decodeBMap(source))

        val id = map.getLong("t")!!
        val messageType = resolveMessageType(map.getString("y")!!)

        return when(messageType) {
            MessageType.error -> {
                val list = map.getList("e")!!
                ErrorMessage(id, list.getInt(0), list.getString(1))
            }
            MessageType.request -> {
                val requestType = map.getString("q")!!
                val marshaller = marshallers.get(resolveRequestTypeByCode(requestType))

                if(marshaller == null)
                    throw IllegalArgumentException()

                marshaller.request.marshall(map.getBMap("a")!!)
            }
            MessageType.response -> {
                val request = requestContainer.findById(id)
                if(request != null && request.mType == MessageType.request) {
                    val marshaller = marshallers.get(request.javaClass)!!.response
                        as BencodeResponseMarshaller<RequestMessage, ResponseMessage<RequestMessage>>

                    val responseData = map.getBMap("r")!!
                    marshaller.marshall(request, responseData)
                } else LostResponse
            }
        }
    }

    private fun resolveRequestTypeByCode(code:String):Class<out RequestMessage> = requestClassesByCodes.get(code)!!

    private fun resolveMessageType(code:String):MessageType = MessageType.values().first { it.code == code }

    public fun registerMarshaller<RQ:RequestMessage, RS:ResponseMessage<RQ>>(marshaller:BencodeMarshaller<RQ, RS>) {

        requestClassesByCodes.put(marshaller.code, marshaller.requestClass)
        codesByRequestClasses.put(marshaller.requestClass, marshaller.code)
        marshallers.put(marshaller.requestClass, marshaller)

    }

    override fun deserialize(message: Message, output: OutputStream) {
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
                    val requestClass = message.javaClass
                    val code = codesByRequestClasses.get(requestClass)

                    value("y", code)

                    val marshaller = marshallers.get(requestClass)!!.request
                        as BencodeRequestMarshaller<RequestMessage>

                    map("a", marshaller.unmarshall(message as RequestMessage))
                }
                MessageType.response -> {
                    val requestClass = (message as ResponseMessage<RequestMessage>).request.javaClass

                    val marshaller = marshallers.get(requestClass)!!.response
                        as BencodeResponseMarshaller<RequestMessage, ResponseMessage<RequestMessage>>

                    map("r", marshaller.unmarshall(message as ResponseMessage<RequestMessage>))
                }
            }
        }

        Bencoder.encodeBType(output, result)
    }

}