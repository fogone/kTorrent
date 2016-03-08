package ru.nobirds.torrent.parser

import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.AbstractHttpMessageConverter
import ru.nobirds.torrent.bencode.BMap
import java.io.ByteArrayInputStream

class BEncodeHttpMessageConverter() : AbstractHttpMessageConverter<BMap>(MediaType.ALL) {

    override fun supports(clazz: Class<out Any?>?): Boolean {
        return clazz != null && BMap::class.java.isAssignableFrom(clazz)
    }

    override fun readInternal(clazz: Class<out BMap>?, inputMessage: HttpInputMessage): BMap {
        val bytes = inputMessage.body.buffered().readBytes()
        return Bencoder.decodeBMap(ByteArrayInputStream(bytes))
    }

    override fun writeInternal(t: BMap, outputMessage: HttpOutputMessage) {
        Bencoder.encodeBType(outputMessage.body, t)
    }

}