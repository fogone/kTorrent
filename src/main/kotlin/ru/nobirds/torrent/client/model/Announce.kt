package ru.nobirds.torrent.client.model

import java.net.URL
import java.util.Collections
import java.util.ArrayList

public data class Announce(
        val url:String,
        val additional:List<String> = ArrayList()) {

    public val allUrls:Set<String> = (additional + url).toSet()

    public fun equals(other:Announce):Boolean {
        if(!other.url.equals(url)) return false
        if(!other.additional.equals(additional)) return false

        return true
    }
}
