package ru.nobirds.torrent.client.parser

import java.util.Date
import ru.nobirds.torrent.nullOr
import java.math.BigInteger
import ru.nobirds.torrent.asString

class MapHelper(val map:Map<String, Any>) {

    fun get<T>(key:String, cast:(Any)->T):T? {
        val value = map[key]
        return if(value != null) cast(value)
        else null
    }

    fun getMap(key:String):MapHelper? = get(key) { MapHelper(it as Map<String, Any>) }

    fun getList(key:String):List<Any>? = get(key) { it as List<Any> }

    fun getListOfMaps(key:String):List<MapHelper>? = getList(key).nullOr { map { MapHelper(it as Map<String, Any>) } }

    fun getStrings(key:String):List<String>? = getList(key).nullOr { map { (it as ByteArray).asString() } }

    fun getBytes(key:String):ByteArray? = get(key) { it as ByteArray }

    fun getString(key:String):String? = getBytes(key).nullOr { (this as ByteArray).asString() }

    fun getBigInteger(key:String):BigInteger? = get(key) { it as BigInteger }

    fun getLong(key:String):Long? = getBigInteger(key).nullOr { longValue() }

    fun getDate(key:String):Date? = getLong(key).nullOr { Date(this) }

}