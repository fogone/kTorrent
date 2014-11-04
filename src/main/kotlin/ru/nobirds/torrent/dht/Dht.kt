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
import java.util.Collections
import ru.nobirds.torrent.dht.message.AbstractErrorMessage
import ru.nobirds.torrent.dht.message.ErrorMessageResponse
import ru.nobirds.torrent.dht.message.BootstrapFindNodeRequest
import java.util.concurrent.CopyOnWriteArrayList

public class Dht(val port:Int) {

    private val requestContainer = DefaultRequestContainer()

    private val peersByHash = ConcurrentHashMap<Id, MutableMap<Id, Peer>>()

    private val tokens = TokenProvider()

    private val localPeer = Peer(Id.random(), InetSocketAddress.createUnresolved("localhost", port))

    private val peers = SimpleKBucket(localPeer.id)

    private val messageFactory = MessageFactory(localPeer)

    private val messageSerializer = BencodeMessageSerializer(localPeer, requestContainer)

    public val server: DhtServer = DhtServer(port, messageSerializer)
            .registerSendListener { onSendMessage(it)}
            .registerReceiveListener { onReceiveMessage(it)}

    private val listeners = ConcurrentHashMap<Id, MutableList<(InetSocketAddress)->Unit>>()

    private val postponedActions = CopyOnWriteArrayList<Dht.()->Unit>()

    private var initialized: Boolean = false

    ;{ initialize() }

    private fun initialize() {
        server.start()
        server.sendTo(*Bootstrap.addresses) { messageFactory.createBootstrapFindNodeRequest(localPeer.id) }
    }

    public fun findPeersForHash(hash: Id, callback:(InetSocketAddress)->Unit) {
        processAction {
            if (peers.containsValue(hash)) {
                peers.getValue(hash).forEach { callback(it) }
            }

            listeners.getOrPut(hash) { ArrayList() }.add(callback)

            server.send(peers.findClosest(hash)) { messageFactory.createGetPeersRequest(hash) }
        }
    }

    private fun processAction(action:Dht.()-> Unit) {
        if(initialized)
            action()
        else
            postponedActions.add(action)
    }

    private fun onSendMessage(addressAndMessage: AddressAndMessage) {
        val message = addressAndMessage.message
        if(message is RequestMessage)
            requestContainer.storeWithTimeout(message) {
                tryResend(addressAndMessage)
            }
    }

    private fun tryResend(addressAndMessage: AddressAndMessage) {
        server.sendTo(addressAndMessage.address) { addressAndMessage.message } // todo
    }

    private fun onReceiveMessage(message: Message) {
        when(message) {
            is ResponseMessage -> {
                onAnswer(message)
            }
            is AbstractErrorMessage -> {
                onErrorReceive(message)
            }
            is RequestMessage -> {
                onRequestReceive(message)
            }
        }
    }

    private fun onRequestReceive(request:RequestMessage) {
        when(request) {
            is PingRequest -> {
                server.sendTo(request.sender.address) { messageFactory.createPingResponse(request) }
            }
            is GetPeersRequest -> {
                val token = tokens.getPeerToken(request.sender.id)

                val response = if (peers.containsValue(request.hash)) {
                    val nodes = peers.getValue(request.hash)
                    messageFactory.createPeersFoundResponse(request, token, nodes)
                } else {
                    val closest = peers.findClosest(request.hash)
                    messageFactory.createClosestNodesResponse(request, token, closest)
                }

                server.sendTo(request.sender.address) { response }
            }
            is FindNodeRequest -> {
                val target = request.target

                val responsePeers = if (peers.containsNode(target)) {
                    Collections.singletonList(peers.getNode(target))
                } else {
                    peers.findClosest(target)
                }

                server.sendTo(request.sender.address) { messageFactory.createFindNodeResponse(request, responsePeers) }
            }
            is AnnouncePeerRequest -> {
                if(!tokens.checkPeerToken(request.sender, request.token))
                    server.sendTo(request.sender.address) { messageFactory.errors.generic("Invalid token") }
                else {
                    val hash = request.hash

                    val nodes = peersByHash.getOrPut(hash) { ConcurrentHashMap() }

                    nodes[request.sender.id] = request.sender

                    server.sendTo(request.sender.address) { messageFactory.createAnnouncePeerResponse(request) }
                }
            }
        }
    }

    private fun onAnswer(response:ResponseMessage) {
        val request = response.request

        when (request) {
            is PingRequest -> {
                peers.addNode(response.sender)
            }
            is GetPeersRequest -> {
                when (response) {
                    is ClosestNodesResponse -> {
                        peers.addNodes(response.nodes)
                        server.sendTo(response.nodes.map { it.address }) { messageFactory.createGetPeersRequest(request.hash) }
                    }
                    is PeersFoundResponse -> {
                        peers.putValue(request.hash, response.nodes)
                        for (listener in listeners.getOrDefault(request.hash, Collections.emptyList())) {
                            response.nodes.forEach {
                                listener(it)
                            }
                        }
                    }
                }
            }

            is FindNodeRequest -> {
                val findNodeResponse = response as FindNodeResponse
                peers.addNodes(findNodeResponse.nodes)

                if(request is BootstrapFindNodeRequest && !initialized) {
                    initialized = true
                    postponedActions.forEach { it() }
                }
            }
        }
    }

    private fun onErrorReceive(message:AbstractErrorMessage) {
        when (message) {
            is ErrorMessageResponse -> {
                when(message.request) {
                    is PingRequest -> {
                        peers.removeNode(message.sender.id)
                    }
                }
            }
        }

    }

}