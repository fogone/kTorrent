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

public abstract class Message(val id:String, val mType:MessageType, val sender:Node)

public data class ErrorMessage(id:String, source:InetSocketAddress, val error:Int, val message:String): Message(id, MessageType.error, Node(Id.Zero, source))

public abstract class AbstractRoutingMessage(id:String, mType:MessageType, sender:Node) : Message(id, mType, sender)

public abstract class RequestMessage(id:String, sender:Node) : AbstractRoutingMessage(id, MessageType.request, sender)

public abstract class ResponseMessage(id:String, sender:Node) : AbstractRoutingMessage(id, MessageType.response, sender)

public data class PingRequest(id:String, sender:Node) : RequestMessage(id, sender)
public data class PingResponse(id:String, sender:Node) : ResponseMessage(id, sender)

public data class AnnouncePeerRequest(
        id:String, sender:Node,
        val hash:Id, val impliedPort:Boolean,
        val port:Int?, val token:String) : RequestMessage(id, sender)

public data class AnnouncePeerResponse(id:String, sender:Node) : ResponseMessage(id, sender)

public data class FindNodeRequest(id:String, sender:Node, val target:Id) : RequestMessage(id, sender)
public data class FindNodeResponse(id:String, sender:Node, val nodes:List<InetSocketAddress>) : ResponseMessage(id, sender)

public data class GetPeersRequest(id:String, sender:Node, val hash:Id) : RequestMessage(id, sender)
public abstract class GetPeersResponse(id:String, sender:Node, val token:String, val nodes:List<InetSocketAddress>) : ResponseMessage(id, sender)

public data class PeersFoundResponse(id:String, sender:Node, token:String, nodes:List<InetSocketAddress>) : GetPeersResponse(id, sender, token, nodes)
public data class ClosestPeersResponse(id:String, sender:Node, token:String, nodes:List<InetSocketAddress>) : GetPeersResponse(id, sender, token, nodes)

public data class LostResponse(source:InetSocketAddress) : ResponseMessage("0", Node(Id.Zero, source))

class DefaultErrors(val factory:MessageFactory) {

    public fun generic(message:String = "Generic Error"):ErrorMessage = factory.createErrorMessage(201, message)
    public fun server(message:String = "Server Error"):ErrorMessage = factory.createErrorMessage(202, message)
    public fun protocol(message:String = "Protocol Error"):ErrorMessage = factory.createErrorMessage(203, message)
    public fun unknownMethod(message:String = "Method Unknown"):ErrorMessage = factory.createErrorMessage(204, message)

}

public class MessageFactory(val sender:Node, val idSequence:IdSequence = IncrementIdSequence()) {

    private val lostResponse = LostResponse(sender.address)

    public val errors: DefaultErrors = DefaultErrors(this)

    public fun createLostResponse():LostResponse = lostResponse

    public fun createPingRequest(node:Node = sender, id:String = idSequence.next()):PingRequest = PingRequest(id, node)

    public fun createPingResponse(id:String, node:Node = sender):PingResponse = PingResponse(id, node)

    public fun createFindNodeRequest(target:Id, node:Node = sender, id:String = idSequence.next()):FindNodeRequest = FindNodeRequest(id, node, target)

    public fun createFindNodeResponse(id:String, node:Node, nodes:List<InetSocketAddress>):FindNodeResponse = FindNodeResponse(id, node, nodes)

    public fun createGetPeersRequest(hash:Id, node:Node = sender, id:String = idSequence.next()):GetPeersRequest
            = GetPeersRequest(id, node, hash)

    public fun createPeersFoundResponse(id:String, node:Node, token:String, nodes:List<InetSocketAddress>):GetPeersResponse
            = PeersFoundResponse(id, node, token, nodes)

    public fun createClosestPeersResponse(id:String, node:Node, token:String, nodes:List<InetSocketAddress>):GetPeersResponse
            = ClosestPeersResponse(id, node, token, nodes)

    public fun createAnnouncePeerRequest(hash:Id, token:String, node:Node = sender, id:String = idSequence.next()):AnnouncePeerRequest
            = AnnouncePeerRequest(id, node, hash, true, null, token)

    public fun createAnnouncePeerResponse(id:String, node:Node):AnnouncePeerResponse = AnnouncePeerResponse(id, node)

    public fun createErrorMessage(code:Int, message:String, id:String = idSequence.next()):ErrorMessage = ErrorMessage(id, sender.address, code, message)

}