package ru.nobirds.torrent.kademlia.message

import ru.nobirds.torrent.kademlia.Node
import ru.nobirds.torrent.kademlia.Id

public enum class MessageType(val code:String) {

    request:MessageType("q")
    response:MessageType("r")
    error:MessageType("e")

}

public abstract class Message(val id:Long, val mType:MessageType)

public data class ErrorMessage(id:Long, val error:Int, val message:String): Message(id, MessageType.error)

public abstract class RequestMessage(id:Long) : Message(id, MessageType.request)

public abstract class ResponseMessage<R: RequestMessage>(val request:R) : Message(request.id, MessageType.response)

public object LostRequest : RequestMessage(-1)
public object LostResponse : ResponseMessage<RequestMessage>(LostRequest)

public data class PingRequest(id:Long, val pid:String) : RequestMessage(id)
public data class PingResponse(request:PingRequest) : ResponseMessage<PingRequest>(request)

public data class AnnouncePeerRequest(id:Long, val node:Node, val key:String, val value:String) : RequestMessage(id)
public data class AnnouncePeerResponse(request:AnnouncePeerRequest) : ResponseMessage<AnnouncePeerRequest>(request)

public data class FindNodeRequest(id:Long, val node:Node) : RequestMessage(id)
public data class FindNodeResponse(request:FindNodeRequest, val nodes:Array<Node>) : ResponseMessage<FindNodeRequest>(request)

public data class GetPeersRequest(id:Long, val hash:Id) : RequestMessage(id)
public data class GetPeersResponse(request:GetPeersRequest, val nodes:Array<Node>) : ResponseMessage<GetPeersRequest>(request)

