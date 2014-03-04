package ru.nobirds.torrent.client.announce

import org.springframework.http.converter.AbstractHttpMessageConverter
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import ru.nobirds.torrent.client.parser.BencoderService
import org.springframework.http.MediaType

public class BEncodeHttpMessageConverter() : AbstractHttpMessageConverter<Map<String, Any>>(MediaType.ALL) {

    private val bencoder = BencoderService()

    override fun supports(clazz: Class<out Any?>?): Boolean {
        return clazz != null && javaClass<Map<String, Any>>().isAssignableFrom(clazz)
    }

    override fun readInternal(clazz: Class<out Map<String, Any>>?, inputMessage: HttpInputMessage): Map<String, Any> {
        val body = inputMessage.getBody()!!
        return bencoder.decode(body)
    }

    override fun writeInternal(t: Map<String, Any>, outputMessage: HttpOutputMessage) {
        bencoder.encode(outputMessage.getBody()!!, t)
    }

}