package ru.nobirds.torrent.client.message

import java.io.DataOutputStream
import java.io.DataInputStream

trait MessageSerializer<T:Message> {

    fun read(length:Int, messageType:MessageType, stream:DataInputStream):T

    fun write(stream:DataOutputStream, message:T)

}

