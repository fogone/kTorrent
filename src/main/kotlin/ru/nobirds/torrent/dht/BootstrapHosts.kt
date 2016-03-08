package ru.nobirds.torrent.dht

import java.net.InetSocketAddress

object BootstrapHosts {

    val addresses:Array<InetSocketAddress> = arrayOf(
            InetSocketAddress("router.utorrent.com", 6881),
            //InetSocketAddress("router.bittorrent.com", 6881),
            InetSocketAddress("dht.transmissionbt.com", 6881)
    )

}
