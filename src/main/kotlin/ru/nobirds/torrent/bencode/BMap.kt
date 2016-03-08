package ru.nobirds.torrent.bencode

import ru.nobirds.torrent.utils.asString
import java.math.BigInteger
import java.util.Date
import java.util.LinkedHashMap


class BMap(private val children:MutableMap<String, BKeyValuePair> = LinkedHashMap())
    : AbstractBlockBType('d'), MutableMap<String, BKeyValuePair> by children {

    fun getValue(name:String):BType? = get(name)?.value

    fun putValue(name:String, value:BType) {
        put(name, BKeyValuePair().set(name, value))
    }

    fun getOrPutValue(name:String, defaultValue:()->BType):BType
            = getOrPut(name) { BKeyValuePair().set(name, defaultValue()) }.value

    override fun onChar(stream: BTokenStream) {
        val bpair = BKeyValuePair()
        bpair.process(stream)
        children.put(bpair.name, bpair)
    }

    fun <T:Any> get(key:String, cast:(BKeyValuePair)->T):T? {
        val value = get(key)
        return if(value != null) cast(value) else null
    }

    fun getBMap(key:String):BMap? = get(key) {  it.value as BMap }

    fun getMapPair(key:String):BKeyValuePair? = get(key) { it }

    fun getBList(key:String):BList? = get(key) { it.value as BList }

    fun getStrings(key:String):List<String>? = getBList(key)?.map { (it as BBytes).value.asString() }

    fun getBBytes(key:String):BBytes? = get(key) { it.value as BBytes }
    fun getBytes(key:String):ByteArray? = getBBytes(key)?.value

    fun getString(key:String):String? = getBytes(key)?.asString()

    fun getBNumber(key:String):BNumber? = get(key) { it.value as BNumber }
    fun getBigInteger(key:String):BigInteger? = getBNumber(key)?.value

    fun getLong(key:String):Long? = getBigInteger(key)?.toLong()
    fun getInt(key:String):Int? = getBigInteger(key)?.toInt()

    fun getDate(key:String):Date? = getLong(key)?.let { long -> Date(long) }

}