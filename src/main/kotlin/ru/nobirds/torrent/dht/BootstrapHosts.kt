package ru.nobirds.torrent.dht

import java.net.InetSocketAddress

public object BootstrapHosts {

    public val addresses:Array<InetSocketAddress> = arrayOf(
            InetSocketAddress("router.utorrent.com", 6881),
            //InetSocketAddress("router.bittorrent.com", 6881),
            InetSocketAddress("dht.transmissionbt.com", 6881)
    )

}
