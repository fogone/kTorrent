package ru.nobirds.torrent.dht.message

import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.IncrementIdSequence
import ru.nobirds.torrent.utils.IdSequence
import java.net.InetSocketAddress
import ru.nobirds.torrent.peers.Peer

public enum class MessageType(val code:String) {

    request:MessageType("q")
    response:MessageType("r")
    error:MessageType("e")

}

public abstract class Message(val id:String, val mType:MessageType, val sender: Peer)

public data class ErrorMessage(id:String, source:InetSocketAddress, val error:Int, val message:String): Message(id, MessageType.error, Peer(Id.Zero, source))

public abstract class AbstractRoutingMessage(id:String, mType:MessageType, sender: Peer) : Message(id, mType, sender)

public abstract class RequestMessage(id:String, sender: Peer) : AbstractRoutingMessage(id, MessageType.request, sender)

public abstract class ResponseMessage(id:String, sender: Peer) : AbstractRoutingMessage(id, MessageType.response, sender)

public data class PingRequest(id:String, sender: Peer) : RequestMessage(id, sender)
public data class PingResponse(id:String, sender: Peer) : ResponseMessage(id, sender)

public data class AnnouncePeerRequest(
        id:String, sender: Peer,
        val hash: Id, val impliedPort:Boolean,
        val port:Int?, val token:String) : RequestMessage(id, sender)

public data class AnnouncePeerResponse(id:String, sender: Peer) : ResponseMessage(id, sender)

public data class FindNodeRequest(id:String, sender: Peer, val target: Id) : RequestMessage(id, sender)
public data class FindNodeResponse(id:String, sender: Peer, val nodes:List<InetSocketAddress>) : ResponseMessage(id, sender)

public data class GetPeersRequest(id:String, sender: Peer, val hash: Id) : RequestMessage(id, sender)
public abstract class GetPeersResponse(id:String, sender: Peer, val token:String, val nodes:List<InetSocketAddress>) : ResponseMessage(id, sender)

public data class PeersFoundResponse(id:String, sender: Peer, token:String, nodes:List<InetSocketAddress>) : GetPeersResponse(id, sender, token, nodes)
public data class ClosestNodesResponse(id:String, sender: Peer, token:String, nodes:List<InetSocketAddress>) : GetPeersResponse(id, sender, token, nodes)

public data class LostResponse(source:InetSocketAddress) : ResponseMessage("0", Peer(Id.Zero, source))

class DefaultErrors(val factory:MessageFactory) {

    public fun generic(message:String = "Generic Error"):ErrorMessage = factory.createErrorMessage(201, message)
    public fun server(message:String = "Server Error"):ErrorMessage = factory.createErrorMessage(202, message)
    public fun protocol(message:String = "Protocol Error"):ErrorMessage = factory.createErrorMessage(203, message)
    public fun unknownMethod(message:String = "Method Unknown"):ErrorMessage = factory.createErrorMessage(204, message)

}

public class MessageFactory(val sender: Peer, val idSequence:IdSequence = IncrementIdSequence()) {

    private val lostResponse = LostResponse(sender.address)

    public val errors: DefaultErrors = DefaultErrors(this)

    public fun createLostResponse():LostResponse = lostResponse

    public fun createPingRequest(node: Peer = sender, id:String = idSequence.next()):PingRequest = PingRequest(id, node)

    public fun createPingResponse(id:String, node: Peer = sender):PingResponse = PingResponse(id, node)

    public fun createFindNodeRequest(target: Id, node: Peer = sender, id:String = idSequence.next()):FindNodeRequest = FindNodeRequest(id, node, target)

    public fun createFindNodeResponse(id:String, node: Peer = sender, nodes:List<InetSocketAddress>):FindNodeResponse = FindNodeResponse(id, node, nodes)

    public fun createGetPeersRequest(hash: Id, node: Peer = sender, id:String = idSequence.next()):GetPeersRequest
            = GetPeersRequest(id, node, hash)

    public fun createPeersFoundResponse(id:String, node: Peer = sender, token:String, nodes:List<InetSocketAddress>):GetPeersResponse
            = PeersFoundResponse(id, node, token, nodes)

    public fun createClosestNodesResponse(id:String, node: Peer = sender, token:String, nodes:List<InetSocketAddress>):GetPeersResponse
            = ClosestNodesResponse(id, node, token, nodes)

    public fun createAnnouncePeerRequest(hash: Id, token:String, node: Peer = sender, id:String = idSequence.next()):AnnouncePeerRequest
            = AnnouncePeerRequest(id, node, hash, true, null, token)

    public fun createAnnouncePeerResponse(id:String, node: Peer = sender):AnnouncePeerResponse = AnnouncePeerResponse(id, node)

    public fun createErrorMessage(code:Int, message:String, id:String = idSequence.next()):ErrorMessage = ErrorMessage(id, sender.address, code, message)

}