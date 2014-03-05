package ru.nobirds.torrent.client.parser

import org.springframework.http.converter.AbstractHttpMessageConverter
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import ru.nobirds.torrent.client.parser.Bencoder
import org.springframework.http.MediaType
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.asString
import java.io.ByteArrayInputStream
import java.io.FileOutputStream

public class BEncodeHttpMessageConverter() : AbstractHttpMessageConverter<BMap>(MediaType.ALL) {

    override fun supports(clazz: Class<out Any?>?): Boolean {
        return clazz != null && javaClass<BMap>().isAssignableFrom(clazz)
    }

    override fun readInternal(clazz: Class<out BMap>?, inputMessage: HttpInputMessage): BMap {
        val body = inputMessage.getBody()!!
        val bytes = body.buffered().readBytes()
        return Bencoder.decodeBMap(ByteArrayInputStream(bytes))
    }

    override fun writeInternal(t: BMap, outputMessage: HttpOutputMessage) {
        Bencoder.encodeBType(outputMessage.getBody()!!, t)
    }

}