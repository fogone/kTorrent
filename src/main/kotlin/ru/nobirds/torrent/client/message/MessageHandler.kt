package ru.nobirds.torrent.client.message

import ru.nobirds.torrent.client.task.TorrentState

public trait MessageHandler<T:Message> {

    fun handle(message:T)

}

public object DoNothingMessageHandler : MessageHandler<Message> {
    override fun handle(message: Message) {}
}

public class BitFieldMessageHandler(val torrentState: TorrentState) : MessageHandler<BitFieldMessage> {

    override fun handle(message: BitFieldMessage) {
        val state = torrentState.state
        state.clear()
        state.or(message.pieces)
    }

}