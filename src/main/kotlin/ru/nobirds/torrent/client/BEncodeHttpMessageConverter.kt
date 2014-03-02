package ru.nobirds.torrent.client

import org.springframework.http.converter.AbstractHttpMessageConverter
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import ru.nobirds.torrent.client.parser.BencoderService

public class BEncodeHttpMessageConverter() : AbstractHttpMessageConverter<Map<String, Any>>() {

    private val bencoder = BencoderService()

    override fun supports(clazz: Class<out Any?>?): Boolean {
        return clazz != null && javaClass<Map<String, Any>>().isAssignableFrom(clazz)
    }

    override fun readInternal(clazz: Class<out Map<String, Any>>?, inputMessage: HttpInputMessage): Map<String, Any> {
        return bencoder.decode(inputMessage.getBody()!!)
    }

    override fun writeInternal(t: Map<String, Any>, outputMessage: HttpOutputMessage) {
        bencoder.encode(outputMessage.getBody()!!, t)
    }

}