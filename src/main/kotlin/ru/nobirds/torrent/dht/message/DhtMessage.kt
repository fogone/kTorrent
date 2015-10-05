package ru.nobirds.torrent.dht.message

import ru.nobirds.torrent.dht.Peer
import ru.nobirds.torrent.dht.message.MessageType.values
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.IdSequence
import ru.nobirds.torrent.utils.IncrementIdSequence
import java.net.InetSocketAddress

public enum class MessageType(val code:String) {
    request("q"),
    response("r"),
    error("e")
}

private val messageTypeIndex = values().toMap { it.code }
public fun findMessageTypeByCode(code: String): MessageType = messageTypeIndex[code]!!

public enum class RequestType(val code: String) {
    ping("ping"),
    findNode("find_node"),
    findPeer("get_peers"),
    announcePeer("announce_peer"),
    unknown("unknown")
}
private val requestTypeIndex = RequestType.values().toMap { it.code }
public fun findRequestTypeByCode(code: String): RequestType = requestTypeIndex[code]!!

public abstract class DhtMessage(val id:String, val sender: Id, val messageType:MessageType)

public open class AbstractErrorMessage(id:String, val error:Int, val message:String, sender: Id): DhtMessage(id, sender, MessageType.error)

public class ErrorMessage(id:String, error:Int, message:String, sender: Id): AbstractErrorMessage(id, error, message, sender)
public class ErrorMessageResponse(val request: RequestMessage, error:Int, message:String, sender: Id): AbstractErrorMessage(request.id, error, message, sender)

public abstract class AbstractRoutingMessage(id:String, sender: Id, mType:MessageType) : DhtMessage(id, sender, mType)

public abstract class RequestMessage(id:String, sender: Id, val type: RequestType) : AbstractRoutingMessage(id, sender, MessageType.request)

public abstract class ResponseMessage(sender: Id, val request: RequestMessage) : AbstractRoutingMessage(request.id, sender, MessageType.response)

public class PingRequest(id:String, sender: Id) : RequestMessage(id, sender, RequestType.ping)
public class PingResponse(sender: Id, request: PingRequest) : ResponseMessage(sender, request)

public class AnnouncePeerRequest(
        id:String, sender: Id,
        val hash: Id, val impliedPort:Boolean,
        val port:Int?, val token:String) : RequestMessage(id, sender, RequestType.announcePeer)

public class AnnouncePeerResponse(sender: Id, request: AnnouncePeerRequest) : ResponseMessage(sender, request)

public open class FindNodeRequest(id:String, sender: Id, val target: Id) : RequestMessage(id, sender, RequestType.findNode)
public class BootstrapFindNodeRequest(id:String, sender: Id, target: Id) : FindNodeRequest(id, sender, target)

public class FindNodeResponse(sender: Id, request: FindNodeRequest, val nodes:List<Peer>) : ResponseMessage(sender, request)

public class GetPeersRequest(id:String, sender: Id, val hash: Id) : RequestMessage(id, sender, RequestType.findPeer)

public abstract class GetPeersResponse(sender: Id, request: GetPeersRequest, val token:String?) : ResponseMessage(sender, request)

public class PeersFoundResponse(sender: Id, request: GetPeersRequest, token:String?, val nodes:List<InetSocketAddress>) : GetPeersResponse(sender, request, token)
public class ClosestNodesResponse(sender: Id, request: GetPeersRequest, token:String?, val nodes:List<Peer>) : GetPeersResponse(sender, request, token)

public class LostRequest(source: Id) : RequestMessage("0", source, RequestType.unknown)
public class LostResponse(source: Id, request: LostRequest) : ResponseMessage(source, request)

class DefaultErrors(val factory:MessageFactory) {

    public fun generic(message:String = "Generic Error"):ErrorMessage = factory.createErrorMessage(201, message)
    public fun server(message:String = "Server Error"):ErrorMessage = factory.createErrorMessage(202, message)
    public fun protocol(message:String = "Protocol Error"):ErrorMessage = factory.createErrorMessage(203, message)
    public fun unknownMethod(message:String = "Method Unknown"):ErrorMessage = factory.createErrorMessage(204, message)

}

public class MessageFactory(val sender: Id, val idSequence:IdSequence = IncrementIdSequence()) {

    private val lostResponse = LostResponse(sender, LostRequest(sender))

    public val errors: DefaultErrors = DefaultErrors(this)

    public fun createLostResponse():LostResponse = lostResponse

    public fun createPingRequest(node: Id = sender, id:String = idSequence.next()):PingRequest
            = PingRequest(id, node)

    public fun createPingResponse(request: PingRequest,
                                  node: Id = sender):PingResponse
            = PingResponse(node, request)

    public fun createFindNodeRequest(target: Id,
                                     node: Id = sender,
                                     id: String = idSequence.next()):FindNodeRequest
            = FindNodeRequest(id, node, target)

    public fun createBootstrapFindNodeRequest(target: Id,
                                     node: Id = sender,
                                     id: String = idSequence.next()):FindNodeRequest
            = BootstrapFindNodeRequest(id, node, target)

    public fun createFindNodeResponse(request: FindNodeRequest,
                                      nodes: List<Peer>,
                                      node: Id = sender):FindNodeResponse
            = FindNodeResponse(node, request, nodes)

    public fun createGetPeersRequest(hash: Id, node: Id = sender, id:String = idSequence.next()):GetPeersRequest
            = GetPeersRequest(id, node, hash)

    public fun createPeersFoundResponse(request: GetPeersRequest,
                                        token: String?,
                                        nodes: List<InetSocketAddress>,
                                        node: Id = sender):GetPeersResponse
            = PeersFoundResponse(node, request, token, nodes)

    public fun createClosestNodesResponse(request: GetPeersRequest,
                                          token: String?,
                                          nodes: List<Peer>,
                                          node: Id = sender):GetPeersResponse
            = ClosestNodesResponse(node, request, token, nodes)

    public fun createAnnouncePeerRequest(hash: Id, token:String, node: Id = sender, id:String = idSequence.next()):AnnouncePeerRequest
            = AnnouncePeerRequest(id, node, hash, true, null, token)

    public fun createAnnouncePeerResponse(request: AnnouncePeerRequest,
                                          node: Id = sender):AnnouncePeerResponse
            = AnnouncePeerResponse(node, request)

    public fun createErrorMessage(code:Int, message:String, node: Id = sender, id:String = idSequence.next()):ErrorMessage
            = ErrorMessage(id, code, message, node)

    public fun createErrorMessageResponse(request: RequestMessage, code:Int, message:String, node: Id = sender, id:String = idSequence.next()): ErrorMessageResponse
            = ErrorMessageResponse(request, code, message, node)

}