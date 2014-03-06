package ru.nobirds.torrent.client.task.tracker

import ru.nobirds.torrent.client.announce.TorrentNotFoundException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.http.HttpStatus

public class ExceptionMapper {

    public fun map(e:Exception):TrackerStatus = when(e) {
        is TorrentNotFoundException -> TrackerStatus.notFound
        is HttpServerErrorException ->
            if(e.getStatusCode() == HttpStatus.NOT_FOUND) TrackerStatus.notFound else TrackerStatus.error
        else -> TrackerStatus.error
    }

}