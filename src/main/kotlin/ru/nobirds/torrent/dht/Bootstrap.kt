package ru.nobirds.torrent.dht

import java.net.InetSocketAddress

public object Bootstrap {

    public val addresses:Array<InetSocketAddress> = array(
            InetSocketAddress("router.bittorrent.com", 6881),
            InetSocketAddress("router.utorrent.com", 6881),
            InetSocketAddress("dht.transmissionbt.com", 6881)
    )

}
