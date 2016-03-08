package ru.nobirds.torrent.client.model

import java.util.*

data class Announce(
        val url:String,
        val additional:List<String> = ArrayList()) {

    val allUrls:Set<String> = (additional + url).toSet()

    fun equals(other:Announce):Boolean {
        if(!other.url.equals(url)) return false
        if(!other.additional.equals(additional)) return false

        return true
    }
}
