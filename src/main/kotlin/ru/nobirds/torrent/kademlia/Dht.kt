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
import ru.nobirds.torrent.kademlia.message.AnnouncePeerRequest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import ru.nobirds.torrent.kademlia.message.GetPeersRequest

public open class DhtException(message:String) : RuntimeException(message)

public class ErrorAnswerException(code:Int, message:String) : DhtException("error [$code] $message")

public class Dht(val port:Int) {

    private val requestContainer = DefaultRequestContainer()

    private val messageSerializer = BencodeMessageSerializer(requestContainer)

    private val map = ConcurrentHashMap<Id, MutableMap<Id, Node>>()

    private val server:Server = Server(port, messageSerializer)
            .registerSendListener { onSendMessage(it)}
            .registerReceiveListener { onReceiveMessage(it)}

    private val peers = PeersContainer()

    private val messageFactory = MessageFactory(Node(Id.random(), InetSocketAddress.createUnresolved("localhost", 0)))

    public fun findPeersForHash(hash:Id, callback:(Node)->Unit) {
        peers.registerPeerListener(hash, callback)
        server.send(messageFactory.createGetPeersRequest(hash))
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
                    onErrorReceive(request, message)
            }
            is RequestMessage -> {
                onRequestReceive(message)
            }
        }
    }

    private fun onRequestReceive(request:RequestMessage) {
        when(request) {
            is PingRequest -> {
                server.send(messageFactory.createPingResponse(request.id, request.sender))
            }
            is GetPeersRequest -> {
                peers.find(request.hash)
                server.send(messageFactory.createGetPeersRequest())
            }
            is AnnouncePeerRequest -> {
                if(!peers.checkPeerToken(request.sender, request.token))
                    server.send(messageFactory.errors.generic("Invalid token"))
                else {
                    val hash = request.hash

                    val nodes = map.getOrPut(hash) { ConcurrentHashMap() }

                    nodes[request.sender.id] = request.sender

                    server.send(messageFactory.createAnnouncePeerResponse(request.id, request.sender))
                }
            }
        }
    }

    private fun onAnswer(request:RequestMessage, response:ResponseMessage) {

    }

    private fun onErrorReceive(request:RequestMessage, response:ErrorMessage) {

    }
}