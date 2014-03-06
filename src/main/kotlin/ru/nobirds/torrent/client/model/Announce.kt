package ru.nobirds.torrent.client.model

import java.net.URL
import java.util.Collections

public data class Announce(
        val url:String,
        val additional:List<String> = Collections.emptyList()) {

    public val allUrls:Set<String> = (additional + url).toSet()

}
