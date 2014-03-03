package ru.nobirds.torrent.client.message

public trait MessageHandler<T:Message> {

    fun handle(message:T)

}

public object DoNothingMessageHandler : MessageHandler<Message> {
    override fun handle(message: Message) {}
}

public class BitFieldMessageHandler() : MessageHandler<BitFieldMessage> {

    override fun handle(message: BitFieldMessage) {

    }

}