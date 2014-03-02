package ru.nobirds.torrent.client.announce

import java.net.URL

public trait AnnounceListener {

    fun onSchedule(url:URL)

}