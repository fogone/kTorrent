package ru.nobirds.torrent.peers

public trait PeerListener {

    fun onPeerEvent(event: PeerEvent)

}

class PeerListenerWrapper(val listener:(PeerEvent)-> Unit) : PeerListener {
    override fun onPeerEvent(event: PeerEvent) {
        listener(event)
    }
}