package ru.nobirds.torrent.client.model

import java.net.URL
import java.util.Collections

public data class Announce(
        val url:URL,
        val additional:List<URL> = Collections.emptyList()) {

    public val allUrls:List<URL> = additional + url

}
