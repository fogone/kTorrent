package ru.nobirds.torrent.client

import org.springframework.boot.SpringApplication

public fun main(args:Array<String>) {

    val context = SpringApplication.run(array(javaClass<TorrentClientConfiguration>()), args)


}