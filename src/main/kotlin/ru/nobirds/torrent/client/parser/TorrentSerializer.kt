package ru.nobirds.torrent.client.parser

import ru.nobirds.torrent.client.model.Torrent
import java.io.OutputStream
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BTypeFactory
import java.math.BigInteger
import java.io.ByteArrayOutputStream

public class TorrentSerializer {

    public fun serialize(torrent:Torrent, stream:OutputStream) {
        val bmap = torrentToBMap(torrent)
        Bencoder.encodeBType(stream, bmap)
    }

    public fun torrentToBMap(torrent:Torrent):BMap = BTypeFactory.createBMap() {
        value("creation date", torrent.created)
        value("created by", torrent.createdBy)
        value("comment", torrent.comment)

        value("announce", torrent.announce.url)

        if(!torrent.announce.additional.empty)
        list("announce-list") {
            list {
                for (url in torrent.announce.additional) {
                    value(url)
                }
            }
        }

        val info = torrent.info
        map("info") {
            value("piece length", info.pieceLength)

            val buffer = ByteArrayOutputStream()
            for (hash in info.hashes) {
                buffer.write(hash)
            }

            value("pieces", buffer.toByteArray())

            val files = info.files

            value("name", files.name)
            value("length", files.length)

            if(!files.files.empty)
            list("files") {
                for (file in files.files) {
                    map {
                        value("length", file.length)
                        list("path") {
                            for (item in file.path) {
                                value(item)
                            }
                        }
                    }
                }
            }
        }

    }
}