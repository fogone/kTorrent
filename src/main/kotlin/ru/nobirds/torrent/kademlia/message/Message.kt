package ru.nobirds.torrent.kademlia.message

import ru.nobirds.torrent.kademlia.Node
import ru.nobirds.torrent.kademlia.Id
import ru.nobirds.torrent.utils.IncrementIdSequence
import ru.nobirds.torrent.utils.IdSequence
import java.net.InetSocketAddress

public enum class MessageType(val code:String) {

    request:MessageType("q")
    response:MessageType("r")
    error:MessageType("e")

}

public abstract class Message(val id:Long, val mType:MessageType)

public data class ErrorMessage(id:Long, val error:Int, val message:String): Message(id, MessageType.error)

public abstract class AbstractRoutingMessage(id:Long, mType:MessageType, val sender:Id) : Message(id, mType)

public abstract class RequestMessage(id:Long, sender:Id) : AbstractRoutingMessage(id, MessageType.request, sender)

public abstract class ResponseMessage(id:Long, sender:Id) : AbstractRoutingMessage(id, MessageType.response, sender)

public object LostRequest : RequestMessage(-1, Id.Zero)
public object LostResponse : ResponseMessage(-1, Id.Zero)

public data class PingRequest(id:Long, sender:Id) : RequestMessage(id, sender)
public data class PingResponse(id:Long, sender:Id) : ResponseMessage(id, sender)

public data class AnnouncePeerRequest(id:Long, sender:Id, val hash:Id, val impliedPort:Boolean, val port:Int?, val token:String) : RequestMessage(id, sender)
public data class AnnouncePeerResponse(id:Long, sender:Id, val hash:Id) : ResponseMessage(id, sender)

public data class FindNodeRequest(id:Long, sender:Id, val target:Id) : RequestMessage(id, sender)
public data class FindNodeResponse(id:Long, sender:Id, val nodes:List<InetSocketAddress>) : ResponseMessage(id, sender)

public data class GetPeersRequest(id:Long, sender:Id, val hash:Id) : RequestMessage(id, sender)
public abstract class GetPeersResponse(id:Long, sender:Id, val token:String, val nodes:List<InetSocketAddress>) : ResponseMessage(id, sender)

public data class PeersFoundResponse(id:Long, sender:Id, token:String, nodes:List<InetSocketAddress>) : GetPeersResponse(id, sender, token, nodes)
public data class ClosestPeersResponse(id:Long, sender:Id, token:String, nodes:List<InetSocketAddress>) : GetPeersResponse(id, sender, token, nodes)

class DefaultErrors(val factory:MessageFactory) {

    public fun generic(message:String = "Generic Error"):ErrorMessage = factory.createErrorMessage(201, message)
    public fun server(message:String = "Server Error"):ErrorMessage = factory.createErrorMessage(202, message)
    public fun protocol(message:String = "Protocol Error"):ErrorMessage = factory.createErrorMessage(203, message)
    public fun unknownMethod(message:String = "Method Unknown"):ErrorMessage = factory.createErrorMessage(204, message)

}

public class MessageFactory(val sender:Id, val idSequence:IdSequence = IncrementIdSequence()) {

    public val errors: DefaultErrors = DefaultErrors(this)

    public fun createPingRequest(node:Id = sender, id:Long = idSequence.next()):PingRequest = PingRequest(id, node)

    public fun createPingResponse(id:Long, node:Id = sender):PingResponse = PingResponse(id, node)

    public fun createFindNodeRequest(target:Id, node:Id = sender, id:Long = idSequence.next()):FindNodeRequest = FindNodeRequest(id, node, target)

    public fun createFindNodeResponse(id:Long, node:Id = sender, nodes:List<InetSocketAddress>):FindNodeResponse = FindNodeResponse(id, node, nodes)

    public fun createGetPeersRequest(hash:Id, node:Id = sender, id:Long = idSequence.next()):GetPeersRequest
            = GetPeersRequest(id, node, hash)

    public fun createPeersFoundResponse(id:Long, token:String, nodes:List<InetSocketAddress>, node:Id = sender):GetPeersResponse
            = PeersFoundResponse(id, node, token, nodes)

    public fun createClosestPeersResponse(id:Long, token:String, nodes:List<InetSocketAddress>, node:Id = sender):GetPeersResponse
            = ClosestPeersResponse(id, node, token, nodes)

    public fun createAnnouncePeerRequest(hash:Id, token:String, node:Id = sender, id:Long = idSequence.next()):AnnouncePeerRequest
            = AnnouncePeerRequest(id, node, hash, true, null, token)

    public fun createAnnouncePeerResponse(id:Long, hash:Id, node:Id = sender):AnnouncePeerResponse = AnnouncePeerResponse(id, sender, hash)

    public fun createErrorMessage(code:Int, message:String, id:Long = idSequence.next()):ErrorMessage = ErrorMessage(id, code, message)

}