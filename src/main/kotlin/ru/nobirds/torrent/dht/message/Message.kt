package ru.nobirds.torrent.dht.message

import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.IncrementIdSequence
import ru.nobirds.torrent.utils.IdSequence
import java.net.InetSocketAddress
import ru.nobirds.torrent.peers.Peer
import ru.nobirds.torrent.dht.message.MessageType.values

public enum class MessageType(val code:String) {
    request:MessageType("q")
    response:MessageType("r")
    error:MessageType("e")
}
private val messageTypeIndex = values().toMap { it.code }
public fun findMessageTypeByCode(code: String): MessageType = messageTypeIndex[code]!!

public enum class RequestType(val code: String) {
    ping : RequestType("ping")
    findNode : RequestType("find_node")
    findPeer : RequestType("get_peers")
    announcePeer : RequestType("announce_peer")
    unknown : RequestType("unknown")
}
private val requestTypeIndex = RequestType.values().toMap { it.code }
public fun findRequestTypeByCode(code: String): RequestType = requestTypeIndex[code]!!

public abstract class Message(val id:String, val sender: Peer, val messageType:MessageType)

public open class AbstractErrorMessage(id:String, val error:Int, val message:String, sender: Peer): Message(id, sender, MessageType.error)

public data class ErrorMessage(id:String, error:Int, message:String, sender: Peer): AbstractErrorMessage(id, error, message, sender)
public data class ErrorMessageResponse(val request: RequestMessage, error:Int, message:String, sender: Peer): AbstractErrorMessage(request.id, error, message, sender)

public abstract class AbstractRoutingMessage(id:String, sender: Peer, mType:MessageType) : Message(id, sender, mType)

public abstract class RequestMessage(id:String, sender: Peer, val type: RequestType) : AbstractRoutingMessage(id, sender, MessageType.request)

public abstract class ResponseMessage(sender: Peer, val request: RequestMessage) : AbstractRoutingMessage(request.id, sender, MessageType.response)

public data class PingRequest(id:String, sender: Peer) : RequestMessage(id, sender, RequestType.ping)
public data class PingResponse(sender: Peer, request: PingRequest) : ResponseMessage(sender, request)

public data class AnnouncePeerRequest(
        id:String, sender: Peer,
        val hash: Id, val impliedPort:Boolean,
        val port:Int?, val token:String) : RequestMessage(id, sender, RequestType.announcePeer)

public data class AnnouncePeerResponse(sender: Peer, request: AnnouncePeerRequest) : ResponseMessage(sender, request)

public data open class FindNodeRequest(id:String, sender: Peer, val target: Id) : RequestMessage(id, sender, RequestType.findNode)
public data class BootstrapFindNodeRequest(id:String, sender: Peer, target: Id) : FindNodeRequest(id, sender, target)

public data class FindNodeResponse(sender: Peer, request: FindNodeRequest, val nodes:List<Peer>) : ResponseMessage(sender, request)

public data class GetPeersRequest(id:String, sender: Peer, val hash: Id) : RequestMessage(id, sender, RequestType.findPeer)

public abstract class GetPeersResponse(sender: Peer, request: GetPeersRequest, val token:String?) : ResponseMessage(sender, request)

public data class PeersFoundResponse(sender: Peer, request: GetPeersRequest, token:String?, val nodes:List<InetSocketAddress>) : GetPeersResponse(sender, request, token)
public data class ClosestNodesResponse(sender: Peer, request: GetPeersRequest, token:String?, val nodes:List<Peer>) : GetPeersResponse(sender, request, token)

public data class LostRequest(source: Peer) : RequestMessage("0", source, RequestType.unknown)
public data class LostResponse(source: Peer, request: LostRequest) : ResponseMessage(source, request)

class DefaultErrors(val factory:MessageFactory) {

    public fun generic(message:String = "Generic Error"):ErrorMessage = factory.createErrorMessage(201, message)
    public fun server(message:String = "Server Error"):ErrorMessage = factory.createErrorMessage(202, message)
    public fun protocol(message:String = "Protocol Error"):ErrorMessage = factory.createErrorMessage(203, message)
    public fun unknownMethod(message:String = "Method Unknown"):ErrorMessage = factory.createErrorMessage(204, message)

}

public class MessageFactory(val sender: Peer, val idSequence:IdSequence = IncrementIdSequence()) {

    private val lostResponse = LostResponse(sender, LostRequest(sender))

    public val errors: DefaultErrors = DefaultErrors(this)

    public fun createLostResponse():LostResponse = lostResponse

    public fun createPingRequest(node: Peer = sender, id:String = idSequence.next()):PingRequest
            = PingRequest(id, node)

    public fun createPingResponse(request: PingRequest,
                                  node: Peer = sender):PingResponse
            = PingResponse(node, request)

    public fun createFindNodeRequest(target: Id,
                                     node: Peer = sender,
                                     id: String = idSequence.next()):FindNodeRequest
            = FindNodeRequest(id, node, target)

    public fun createBootstrapFindNodeRequest(target: Id,
                                     node: Peer = sender,
                                     id: String = idSequence.next()):FindNodeRequest
            = BootstrapFindNodeRequest(id, node, target)

    public fun createFindNodeResponse(request: FindNodeRequest,
                                      nodes: List<Peer>,
                                      node: Peer = sender):FindNodeResponse
            = FindNodeResponse(node, request, nodes)

    public fun createGetPeersRequest(hash: Id, node: Peer = sender, id:String = idSequence.next()):GetPeersRequest
            = GetPeersRequest(id, node, hash)

    public fun createPeersFoundResponse(request: GetPeersRequest,
                                        token: String?,
                                        nodes: List<InetSocketAddress>,
                                        node: Peer = sender):GetPeersResponse
            = PeersFoundResponse(node, request, token, nodes)

    public fun createClosestNodesResponse(request: GetPeersRequest,
                                          token: String?,
                                          nodes: List<Peer>,
                                          node: Peer = sender):GetPeersResponse
            = ClosestNodesResponse(node, request, token, nodes)

    public fun createAnnouncePeerRequest(hash: Id, token:String, node: Peer = sender, id:String = idSequence.next()):AnnouncePeerRequest
            = AnnouncePeerRequest(id, node, hash, true, null, token)

    public fun createAnnouncePeerResponse(request: AnnouncePeerRequest,
                                          node: Peer = sender):AnnouncePeerResponse
            = AnnouncePeerResponse(node, request)

    public fun createErrorMessage(code:Int, message:String, node: Peer = sender, id:String = idSequence.next()):ErrorMessage
            = ErrorMessage(id, code, message, node)

    public fun createErrorMessageResponse(request: RequestMessage, code:Int, message:String, node: Peer = sender, id:String = idSequence.next()): ErrorMessageResponse
            = ErrorMessageResponse(request, code, message, node)

}