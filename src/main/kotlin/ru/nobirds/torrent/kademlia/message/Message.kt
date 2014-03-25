package ru.nobirds.torrent.kademlia.message

import ru.nobirds.torrent.kademlia.Node

public abstract class Message(val id:Long)

public data class PingMessage(id:Long) : Message(id)

public data class StoreMessage(id:Long, val node:Node, val key:String, val value:String) : Message(id)

public data class FindNodeMessage(id:Long, val node:Node) : Message(id)

public data class FindValueMessage(id:Long, val node:Node) : Message(id)