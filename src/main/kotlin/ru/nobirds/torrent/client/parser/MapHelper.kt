package ru.nobirds.torrent.client.parser

import java.util.Date
import ru.nobirds.torrent.nullOr
import java.math.BigInteger
import ru.nobirds.torrent.asString
import ru.nobirds.torrent.bencode.BMap
import ru.nobirds.torrent.bencode.BType
import ru.nobirds.torrent.bencode.BKeyValuePair
import ru.nobirds.torrent.bencode.BList
import ru.nobirds.torrent.bencode.BBytes
import ru.nobirds.torrent.bencode.BNumber

class MapHelper(val map:BMap) {

    fun get<T>(key:String, cast:(BKeyValuePair)->T):T? {
        val value = map[key]
        return if(value != null) cast(value)
        else null
    }

    fun getBMap(key:String):BMap? = get(key) {  it.value as BMap }

    fun getMap(key:String):MapHelper? = get(key) { MapHelper(it.value as BMap) }

    fun getMapPair(key:String):BKeyValuePair? = get(key) { it }

    fun getList(key:String):BList? = get(key) { it.value as BList }

    fun getListOfMaps(key:String):List<MapHelper>? = getList(key).nullOr { map { MapHelper(it as BMap) } }

    fun getStrings(key:String):List<String>? = getList(key).nullOr { map { (it as BBytes).value.asString() } }

    fun getBBytes(key:String):BBytes? = get(key) { it.value as BBytes }
    fun getBytes(key:String):ByteArray? = getBBytes(key).nullOr { value }

    fun getString(key:String):String? = getBytes(key).nullOr { asString() }

    fun getBNumber(key:String):BNumber? = get(key) { it.value as BNumber }
    fun getBigInteger(key:String):BigInteger? = getBNumber(key).nullOr { value }

    fun getLong(key:String):Long? = getBigInteger(key).nullOr { longValue() }

    fun getDate(key:String):Date? = getLong(key).nullOr { Date(this) }

}