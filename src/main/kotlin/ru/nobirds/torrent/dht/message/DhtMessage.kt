package ru.nobirds.torrent.dht.message

import ru.nobirds.torrent.dht.Peer
import ru.nobirds.torrent.utils.Id
import ru.nobirds.torrent.utils.IdSequence
import ru.nobirds.torrent.utils.IncrementIdSequence
import java.net.InetSocketAddress

enum class MessageType(val code:String) {
    request("q"),
    response("r"),
    error("e")
}

private val messageTypeIndex = MessageType.values().associateBy { it.code }

fun findMessageTypeByCode(code: String): MessageType = messageTypeIndex[code]!!

enum class RequestType(val code: String) {
    ping("ping"),
    findNode("find_node"),
    findPeer("get_peers"),
    announcePeer("announce_peer"),
    unknown("unknown")
}
private val requestTypeIndex = RequestType.values().associateBy { it.code }
fun findRequestTypeByCode(code: String): RequestType = requestTypeIndex[code]!!

abstract class DhtMessage(val id:String, val sender: Id, val messageType:MessageType)

open class AbstractErrorMessage(id:String, val error:Int, val message:String, sender: Id): DhtMessage(id, sender, MessageType.error)

class ErrorMessage(id:String, error:Int, message:String, sender: Id): AbstractErrorMessage(id, error, message, sender)
class ErrorMessageResponse(val request: RequestMessage, error:Int, message:String, sender: Id): AbstractErrorMessage(request.id, error, message, sender)

abstract class AbstractRoutingMessage(id:String, sender: Id, mType:MessageType) : DhtMessage(id, sender, mType)

abstract class RequestMessage(id:String, sender: Id, val type: RequestType) : AbstractRoutingMessage(id, sender, MessageType.request)

abstract class ResponseMessage(sender: Id, val request: RequestMessage) : AbstractRoutingMessage(request.id, sender, MessageType.response)

class PingRequest(id:String, sender: Id) : RequestMessage(id, sender, RequestType.ping)
class PingResponse(sender: Id, request: PingRequest) : ResponseMessage(sender, request)

class AnnouncePeerRequest(
        id:String, sender: Id,
        val hash: Id, val impliedPort:Boolean,
        val port:Int?, val token:String) : RequestMessage(id, sender, RequestType.announcePeer)

class AnnouncePeerResponse(sender: Id, request: AnnouncePeerRequest) : ResponseMessage(sender, request)

open class FindNodeRequest(id:String, sender: Id, val target: Id) : RequestMessage(id, sender, RequestType.findNode)
class BootstrapFindNodeRequest(id:String, sender: Id, target: Id) : FindNodeRequest(id, sender, target)

class FindNodeResponse(sender: Id, request: FindNodeRequest, val nodes:List<Peer>) : ResponseMessage(sender, request)

class GetPeersRequest(id:String, sender: Id, val hash: Id) : RequestMessage(id, sender, RequestType.findPeer)

abstract class GetPeersResponse(sender: Id, request: GetPeersRequest, val token:String?) : ResponseMessage(sender, request)

class PeersFoundResponse(sender: Id, request: GetPeersRequest, token:String?, val nodes:List<InetSocketAddress>) : GetPeersResponse(sender, request, token)
class ClosestNodesResponse(sender: Id, request: GetPeersRequest, token:String?, val nodes:List<Peer>) : GetPeersResponse(sender, request, token)

class LostRequest(source: Id) : RequestMessage("0", source, RequestType.unknown)
class LostResponse(source: Id, request: LostRequest) : ResponseMessage(source, request)

class DefaultErrors(val factory:MessageFactory) {

    fun generic(message:String = "Generic Error"):ErrorMessage = factory.createErrorMessage(201, message)
    fun server(message:String = "Server Error"):ErrorMessage = factory.createErrorMessage(202, message)
    fun protocol(message:String = "Protocol Error"):ErrorMessage = factory.createErrorMessage(203, message)
    fun unknownMethod(message:String = "Method Unknown"):ErrorMessage = factory.createErrorMessage(204, message)

}

class MessageFactory(val sender: Id, val idSequence:IdSequence = IncrementIdSequence()) {

    private val lostResponse = LostResponse(sender, LostRequest(sender))

    val errors: DefaultErrors = DefaultErrors(this)

    fun createLostResponse():LostResponse = lostResponse

    fun createPingRequest(node: Id = sender, id:String = idSequence.next()):PingRequest
            = PingRequest(id, node)

    fun createPingResponse(request: PingRequest,
                                  node: Id = sender):PingResponse
            = PingResponse(node, request)

    fun createFindNodeRequest(target: Id,
                                     node: Id = sender,
                                     id: String = idSequence.next()):FindNodeRequest
            = FindNodeRequest(id, node, target)

    fun createBootstrapFindNodeRequest(target: Id,
                                     node: Id = sender,
                                     id: String = idSequence.next()):FindNodeRequest
            = BootstrapFindNodeRequest(id, node, target)

    fun createFindNodeResponse(request: FindNodeRequest,
                                      nodes: List<Peer>,
                                      node: Id = sender):FindNodeResponse
            = FindNodeResponse(node, request, nodes)

    fun createGetPeersRequest(hash: Id, node: Id = sender, id:String = idSequence.next()):GetPeersRequest
            = GetPeersRequest(id, node, hash)

    fun createPeersFoundResponse(request: GetPeersRequest,
                                        token: String?,
                                        nodes: List<InetSocketAddress>,
                                        node: Id = sender):GetPeersResponse
            = PeersFoundResponse(node, request, token, nodes)

    fun createClosestNodesResponse(request: GetPeersRequest,
                                          token: String?,
                                          nodes: List<Peer>,
                                          node: Id = sender):GetPeersResponse
            = ClosestNodesResponse(node, request, token, nodes)

    fun createAnnouncePeerRequest(hash: Id, token:String, node: Id = sender, id:String = idSequence.next()):AnnouncePeerRequest
            = AnnouncePeerRequest(id, node, hash, true, null, token)

    fun createAnnouncePeerResponse(request: AnnouncePeerRequest,
                                          node: Id = sender):AnnouncePeerResponse
            = AnnouncePeerResponse(node, request)

    fun createErrorMessage(code:Int, message:String, node: Id = sender, id:String = idSequence.next()):ErrorMessage
            = ErrorMessage(id, code, message, node)

    fun createErrorMessageResponse(request: RequestMessage, code:Int, message:String, node: Id = sender, id:String = idSequence.next()): ErrorMessageResponse
            = ErrorMessageResponse(request, code, message, node)

}