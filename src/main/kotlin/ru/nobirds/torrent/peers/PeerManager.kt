package ru.nobirds.torrent.peers

import ru.nobirds.torrent.utils.Id
import java.util.ArrayList
import ru.nobirds.torrent.utils.LinksCounter
import java.util.HashMap
import java.util.HashSet
import java.util.Collections
import ru.nobirds.torrent.peers.provider.PeerProvider

public class PeerManager : PeerProvider {

    private val providers = ArrayList<PeerProvider>()

    public fun registerProvider(peerProvider: PeerProvider) {
        providers.add(peerProvider)
    }

    override fun require(hash: Id, listener: PeerListener) {
        for (provider in providers) {
            provider.require(hash, listener)
        }
    }

    override fun needless(hash: Id) {
        for (provider in providers) {
            provider.needless(hash)
        }
    }

}