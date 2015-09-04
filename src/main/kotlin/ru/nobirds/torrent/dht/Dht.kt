package ru.nobirds.torrent.dht

import ru.nobirds.torrent.dht.message.*
import ru.nobirds.torrent.dht.message.bencode.BencodeMessageSerializer
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.infiniteLoopThread
import ru.nobirds.torrent.utils.isPortAvailable
import java.io.Closeable
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

public class Dht(val port:Int,val bootstrap:Sequence<InetSocketAddress>) : Closeable {

    private val requestContainer = DefaultRequestContainer()

    private val tokens = TokenProvider()

    private val localHash = Id.random()

    private val peers: KBucket = SimpleKBucket(localHash)

    private val messageFactory = MessageFactory(localHash)

    private val messageSerializer = BencodeMessageSerializer(localHash, requestContainer)

    private val server = NettyDhtServer(port, messageSerializer, requestContainer)

    private val listeners = ConcurrentHashMap<Id, MutableList<(InetSocketAddress)->Unit>>()

    private val postponedActions = CopyOnWriteArrayList<Dht.()->Unit>()

    private var initialized: Boolean = false

    init { initialize() }

    private fun initialize() {
        bootstrap.forEach {
            server.send(AddressAndMessage(it, messageFactory.createBootstrapFindNodeRequest(localHash)))
        }

        infiniteLoopThread {
            handleMessage(server.read())
        }
    }

    public fun findPeersForHash(hash: Id, callback:(InetSocketAddress)->Unit) {
        processAction {
            if (peers.containsValue(hash)) {
                peers.getValue(hash).forEach { callback(it) }
            }

            listeners.concurrentGetOrPut(hash) { ArrayList() }.add(callback)

            peers.findClosest(hash).forEach {
                server.send(it.address, messageFactory.createGetPeersRequest(hash))
            }
        }
    }

    public fun announce(hash: Id) {
        processAction {
            peers.findClosest(hash).forEach {
                server.send(it.address, messageFactory.createAnnouncePeerRequest(hash, tokens.getLocalToken()))
            }
        }
    }

    public fun makeInitialized() {
        if (!initialized) {
            initialized = true
            postponedActions.forEach { it() }
        }
    }

    private fun processAction(action:Dht.()-> Unit) {
        if(initialized)
            action()
        else
            postponedActions.add(action)
    }

    private fun handleMessage(message: AddressAndMessage) {
        when(message.message) {
            is ResponseMessage -> {
                onAnswer(message.message, message.address)
            }
            is AbstractErrorMessage -> {
                onErrorReceive(message.message, message.address)
            }
            is RequestMessage -> {
                onRequestReceive(message.message, message.address)
            }
        }
    }

    private fun onRequestReceive(request:RequestMessage, address: InetSocketAddress) {
        when(request) {
            is PingRequest -> {
                server.send(address, messageFactory.createPingResponse(request))
            }
            is GetPeersRequest -> {
                val token = tokens.getPeerToken(request.sender)

                val response = if (peers.containsValue(request.hash)) {
                    val nodes = peers.getValue(request.hash)
                    messageFactory.createPeersFoundResponse(request, token, nodes)
                } else {
                    val closest = peers.findClosest(request.hash)
                    messageFactory.createClosestNodesResponse(request, token, closest)
                }

                server.send(address, response)
            }
            is FindNodeRequest -> {
                val target = request.target

                val responsePeers = if (peers.containsNode(target)) {
                    Collections.singletonList(peers.getNode(target)!!)
                } else {
                    peers.findClosest(target)
                }

                server.send(address, messageFactory.createFindNodeResponse(request, responsePeers))
            }
            is AnnouncePeerRequest -> {
                if(!tokens.checkPeerToken(request.sender, request.token))
                    server.send(address, messageFactory.errors.generic("Invalid token"))
                else {
                    peers.putValue(request.hash, Collections.singleton(address))
                    server.send(address, messageFactory.createAnnouncePeerResponse(request))
                }
            }
        }
    }

    private fun onAnswer(response: ResponseMessage, address: InetSocketAddress) {
        val request = response.request

        when (request) {
            is PingRequest -> {
                peers.addNode(Peer(response.sender, address))
            }
            is GetPeersRequest -> {
                when (response) {
                    is ClosestNodesResponse -> {
                        peers.addNodes(response.nodes)
                        response.nodes.map { it.address }.forEach {
                            server.send(it, messageFactory.createGetPeersRequest(request.hash))
                        }
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
                    makeInitialized()
                }
            }
        }
    }

    private fun onErrorReceive(message:AbstractErrorMessage, address: InetSocketAddress) {
        when (message) {
            is ErrorMessageResponse -> {
                when(message.request) {
                    is PingRequest -> {
                        peers.removeNode(message.sender)
                    }
                }
            }
        }

    }

    override fun close() {

        server.close()
    }
}