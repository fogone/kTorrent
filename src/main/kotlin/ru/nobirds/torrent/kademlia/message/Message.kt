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

public abstract class Message(val id:String, val mType:MessageType)

public data class ErrorMessage(id:String, val error:Int, val message:String): Message(id, MessageType.error)

public abstract class AbstractRoutingMessage(id:String, mType:MessageType, val sender:Id) : Message(id, mType)

public abstract class RequestMessage(id:String, sender:Id) : AbstractRoutingMessage(id, MessageType.request, sender)

public abstract class ResponseMessage(id:String, sender:Id) : AbstractRoutingMessage(id, MessageType.response, sender)

public object LostRequest : RequestMessage("0", Id.Zero)
public object LostResponse : ResponseMessage("0", Id.Zero)

public data class PingRequest(id:String, sender:Id) : RequestMessage(id, sender)
public data class PingResponse(id:String, sender:Id) : ResponseMessage(id, sender)

public data class AnnouncePeerRequest(id:String, sender:Id, val hash:Id, val impliedPort:Boolean, val port:Int?, val token:String) : RequestMessage(id, sender)
public data class AnnouncePeerResponse(id:String, sender:Id) : ResponseMessage(id, sender)

public data class FindNodeRequest(id:String, sender:Id, val target:Id) : RequestMessage(id, sender)
public data class FindNodeResponse(id:String, sender:Id, val nodes:List<InetSocketAddress>) : ResponseMessage(id, sender)

public data class GetPeersRequest(id:String, sender:Id, val hash:Id) : RequestMessage(id, sender)
public abstract class GetPeersResponse(id:String, sender:Id, val token:String, val nodes:List<InetSocketAddress>) : ResponseMessage(id, sender)

public data class PeersFoundResponse(id:String, sender:Id, token:String, nodes:List<InetSocketAddress>) : GetPeersResponse(id, sender, token, nodes)
public data class ClosestPeersResponse(id:String, sender:Id, token:String, nodes:List<InetSocketAddress>) : GetPeersResponse(id, sender, token, nodes)

class DefaultErrors(val factory:MessageFactory) {

    public fun generic(message:String = "Generic Error"):ErrorMessage = factory.createErrorMessage(201, message)
    public fun server(message:String = "Server Error"):ErrorMessage = factory.createErrorMessage(202, message)
    public fun protocol(message:String = "Protocol Error"):ErrorMessage = factory.createErrorMessage(203, message)
    public fun unknownMethod(message:String = "Method Unknown"):ErrorMessage = factory.createErrorMessage(204, message)

}

public class MessageFactory(val sender:Id, val idSequence:IdSequence = IncrementIdSequence()) {

    public val errors: DefaultErrors = DefaultErrors(this)

    public fun createPingRequest(node:Id = sender, id:String = idSequence.next()):PingRequest = PingRequest(id, node)

    public fun createPingResponse(id:String, node:Id = sender):PingResponse = PingResponse(id, node)

    public fun createFindNodeRequest(target:Id, node:Id = sender, id:String = idSequence.next()):FindNodeRequest = FindNodeRequest(id, node, target)

    public fun createFindNodeResponse(id:String, node:Id = sender, nodes:List<InetSocketAddress>):FindNodeResponse = FindNodeResponse(id, node, nodes)

    public fun createGetPeersRequest(hash:Id, node:Id = sender, id:String = idSequence.next()):GetPeersRequest
            = GetPeersRequest(id, node, hash)

    public fun createPeersFoundResponse(id:String, token:String, nodes:List<InetSocketAddress>, node:Id = sender):GetPeersResponse
            = PeersFoundResponse(id, node, token, nodes)

    public fun createClosestPeersResponse(id:String, token:String, nodes:List<InetSocketAddress>, node:Id = sender):GetPeersResponse
            = ClosestPeersResponse(id, node, token, nodes)

    public fun createAnnouncePeerRequest(hash:Id, token:String, node:Id = sender, id:String = idSequence.next()):AnnouncePeerRequest
            = AnnouncePeerRequest(id, node, hash, true, null, token)

    public fun createAnnouncePeerResponse(id:String, node:Id = sender):AnnouncePeerResponse = AnnouncePeerResponse(id, node)

    public fun createErrorMessage(code:Int, message:String, id:String = idSequence.next()):ErrorMessage = ErrorMessage(id, code, message)

}