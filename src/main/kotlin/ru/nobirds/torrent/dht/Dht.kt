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
import ru.nobirds.torrent.dht.message.bencode.AnnouncePeerBencodeMarshaller
import ru.nobirds.torrent.dht.message.bencode.FindNodeBencodeMarshaller
import ru.nobirds.torrent.dht.message.bencode.PingBencodeMarshaller
import ru.nobirds.torrent.dht.message.bencode.GetPeersBencodeMarshaller
import ru.nobirds.torrent.dht.message.ClosestNodesResponse
import ru.nobirds.torrent.dht.message.PeersFoundResponse
import java.util.ArrayList

public class Dht(val port:Int) {

    private val requestContainer = DefaultRequestContainer()

    private val messageSerializer = BencodeMessageSerializer(requestContainer)

    private val peersByHash = ConcurrentHashMap<Id, MutableMap<Id, Peer>>()

    public val server: DhtServer = DhtServer(port, messageSerializer)
            .registerSendListener { onSendMessage(it)}
            .registerReceiveListener { onReceiveMessage(it)}

    private val tokens = TokenContainer()

    private val peers = DistributedMap()

    private val localId = Id.random()
    private val localPeer = Peer(localId, InetSocketAddress.createUnresolved("localhost", port))
    private val token = tokens.generateToken()

    private val messageFactory = MessageFactory(localPeer)

    private val listeners = ConcurrentHashMap<Id, MutableList<(InetSocketAddress)->Unit>>()

    public fun initialize() {
        server.start {
            sendTo(messageFactory.createFindNodeRequest(localId), *Bootstrap.addresses)
        }
    }

    public fun findPeersForHash(hash: Id, callback:(InetSocketAddress)->Unit) {
        if (peers.contains(hash)) {
            val peers = peers.get(hash)
            if (peers.notEmpty) {
                peers.forEach { callback(it) }
                return
            }
        }

        listeners.getOrPut(hash) { ArrayList() }.add(callback)

        server.send(peers.findClosest(hash), messageFactory.createGetPeersRequest(hash))
    }

    private fun onSendMessage(addressAndMessage: AddressAndMessage) {
        val message = addressAndMessage.message
        if(message is RequestMessage)
            requestContainer.storeWithTimeout(message) {
                tryResend(addressAndMessage)
            }
    }

    private fun tryResend(addressAndMessage: AddressAndMessage) {
        server.sendTo(addressAndMessage.message, addressAndMessage.address) // todo
    }

    private fun onReceiveMessage(message: Message) {
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
                server.sendTo(messageFactory.createPingResponse(request.id), request.sender.address)
            }
            is GetPeersRequest -> {
                val token = tokens.getPeerToken(request.sender.id)
                val nodes = peers.get(request.hash)

                val response = if(nodes.empty)
                    messageFactory.createClosestNodesResponse(request.id, token = token, nodes = peers.findClosest(request.hash).map { it.address })
                else
                    messageFactory.createPeersFoundResponse(request.id, token = token, nodes = nodes.map { it })

                server.sendTo(response, request.sender.address)
            }
            is FindNodeRequest -> {
                val target = request.target
                val peers = peers.get(target)

                server.sendTo(messageFactory.createFindNodeResponse(request.id, nodes = peers.map { it }), request.sender.address)
            }
            is AnnouncePeerRequest -> {
                if(!tokens.checkPeerToken(request.sender, request.token))
                    server.sendTo(messageFactory.errors.generic("Invalid token"), request.sender.address)
                else {
                    val hash = request.hash

                    val nodes = peersByHash.getOrPut(hash) { ConcurrentHashMap() }

                    nodes[request.sender.id] = request.sender

                    server.sendTo(messageFactory.createAnnouncePeerResponse(request.id, request.sender), request.sender.address)
                }
            }
        }
    }

    private fun onAnswer(request:RequestMessage, response:ResponseMessage) {
        when (request) {
            is PingRequest -> {
                peers.addNode(response.sender)
            }
            is GetPeersRequest -> {
                when (response) {
                    is ClosestNodesResponse -> {
                        server.sendTo(messageFactory.createGetPeersRequest(request.hash), response.nodes)
                    }
                    is PeersFoundResponse -> {
                        peers.put(request.hash, response.nodes)
                    }
                }
            }
        }
    }

    private fun onErrorReceive(request:RequestMessage, response:ErrorMessage) {
        when (request) {
            is PingRequest -> {
                peers.removeNode(response.sender.id)
            }
        }
    }

}