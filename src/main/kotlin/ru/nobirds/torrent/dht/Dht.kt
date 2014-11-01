package ru.nobirds.torrent.dht

import ru.nobirds.torrent.dht.message.MessageFactory
import java.net.InetSocketAddress
import ru.nobirds.torrent.dht.message.bencode.BencodeMessageSerializer
import ru.nobirds.torrent.dht.message.DefaultRequestContainer
import ru.nobirds.torrent.dht.message.RequestMessage
import ru.nobirds.torrent.dht.message.ResponseMessage
import ru.nobirds.torrent.dht.message.Message
import ru.nobirds.torrent.dht.message.ErrorMessage
import ru.nobirds.torrent.dht.message.PingRequest
import ru.nobirds.torrent.dht.message.AnnouncePeerRequest
import java.util.concurrent.ConcurrentHashMap
import ru.nobirds.torrent.dht.message.GetPeersRequest
import ru.nobirds.torrent.dht.message.FindNodeRequest
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.dht.message.FindNodeResponse

public open class DhtException(message:String) : RuntimeException(message)

public class ErrorAnswerException(code:Int, message:String) : DhtException("error [$code] $message")

public class Dht(val port:Int) {

    private val requestContainer = DefaultRequestContainer()

    private val messageSerializer = BencodeMessageSerializer(requestContainer)

    private val peersByHash = ConcurrentHashMap<Id, MutableMap<Id, Peer>>()

    private val server:Server = Server(port, messageSerializer)
            .registerSendListener { onSendMessage(it)}
            .registerReceiveListener { onReceiveMessage(it)}

    private val peers = PeersContainer()

    private val messageFactory = MessageFactory(Peer(Id.random(), InetSocketAddress.createUnresolved("localhost", 0)))

    public fun findPeersForHash(hash: Id, callback:(Peer)->Unit) {
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
                val token = peers.getPeerToken(request.sender.id)
                val nodes = peers.find(request.hash)

                val response = if(nodes.empty)
                    messageFactory.createClosestPeersResponse(request.id, request.sender, token, peers.findClosest(request.hash).map { it.address })
                else
                    messageFactory.createPeersFoundResponse(request.id, request.sender, token, nodes.map { it.address })

                server.send(response)
            }
            is FindNodeRequest -> {
                val target = request.target
                val peers = peers.find(target)

                server.send(messageFactory.createFindNodeResponse(request.id, request.sender, peers.map { it.address }))
            }
            is AnnouncePeerRequest -> {
                if(!peers.checkPeerToken(request.sender, request.token))
                    server.send(messageFactory.errors.generic("Invalid token"))
                else {
                    val hash = request.hash

                    val nodes = peersByHash.getOrPut(hash) { ConcurrentHashMap() }

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