package ru.nobirds.torrent.kademlia

import ru.nobirds.torrent.kademlia.message.MessageFactory
import java.net.InetSocketAddress
import ru.nobirds.torrent.kademlia.message.bencode.BencodeMessageSerializer
import ru.nobirds.torrent.kademlia.message.DefaultRequestContainer
import ru.nobirds.torrent.kademlia.message.RequestMessage
import ru.nobirds.torrent.kademlia.message.ResponseMessage
import ru.nobirds.torrent.kademlia.message.Message
import ru.nobirds.torrent.kademlia.message.ErrorMessage
import ru.nobirds.torrent.kademlia.message.PingRequest

public open class DhtException(message:String) : RuntimeException(message)

public class ErrorAnswerException(code:Int, message:String) : DhtException("error [$code] $message")

public class Dht(val port:Int) {

    private val requestContainer = DefaultRequestContainer()

    private val messageSerializer = BencodeMessageSerializer(requestContainer)

    private val server:Server = Server(port, messageSerializer)
            .registerSendListener { onSendMessage(it)}
            .registerReceiveListener { onReceiveMessage(it)}

    private val messageFactory = MessageFactory(Id.random())

    public fun findPeersForHash(hash:Id, callback:(List<InetSocketAddress>)->Unit) {
        val request = messageFactory.createGetPeersRequest(hash)

        server.send(request)
    }

    private fun onSendMessage(message:Message) {
        if(message is RequestMessage)
            requestContainer.storeWithTimeout(message) {
                tryResend(message)
            }
    }

    private fun tryResend(request:RequestMessage) {
        server.send(request) // todo
    }

    private fun onReceiveMessage(message:Message) {
        when(message) {
            is ResponseMessage -> {

                val request = requestContainer.findById(message.id)

                if (request != null)
                    onAnswer(request, message)

            }
            is ErrorMessage -> {

                val request = requestContainer.findById(message.id)

                if(request != null)
                    onError(request, message)
            }
            is RequestMessage -> {
                onRequest(message)
            }
        }
    }

    private fun onRequest(request:RequestMessage) {
        when(request) {
            is PingRequest -> {
                server.send(messageFactory.createPingResponse(request.id))
            }
        }
    }

    private fun onAnswer(request:RequestMessage, response:ResponseMessage) {

    }

    private fun onError(request:RequestMessage, response:ErrorMessage) {

    }
}