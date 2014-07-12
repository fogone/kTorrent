package ru.nobirds.torrent.bencode

import java.util.Date
import java.math.BigInteger
import ru.nobirds.torrent.utils.nullOr
import ru.nobirds.torrent.utils.asString

public class BMapHelper(val map:BMap) {

    fun get<T>(key:String, cast:(BKeyValuePair)->T):T? {
        val value = map[key]
        return if(value != null) cast(value)
        else null
    }

    fun containsKey(key:String):Boolean = map.containsKey(key)

    fun getBMap(key:String):BMap? = get(key) {  it.value as BMap }

    fun getMap(key:String): BMapHelper? = get(key) { BMapHelper(it.value as BMap) }

    fun getMapPair(key:String):BKeyValuePair? = get(key) { it }

    fun getBList(key:String):BList? = get(key) { it.value as BList }

    fun getList(key:String):BListHelper? = get(key) { BListHelper(it.value as BList) }

    fun getListOfMaps(key:String):List<BMapHelper>? = getBList(key).nullOr { map { BMapHelper(it as BMap) } }

    fun getStrings(key:String):List<String>? = getBList(key).nullOr { map { (it as BBytes).value.asString() } }

    fun getBBytes(key:String):BBytes? = get(key) { it.value as BBytes }
    fun getBytes(key:String):ByteArray? = getBBytes(key).nullOr { value }

    fun getString(key:String):String? = getBytes(key).nullOr { asString() }

    fun getBNumber(key:String):BNumber? = get(key) { it.value as BNumber }
    fun getBigInteger(key:String):BigInteger? = getBNumber(key).nullOr { value }

    fun getLong(key:String):Long? = getBigInteger(key).nullOr { longValue() }
    fun getInt(key:String):Int? = getBigInteger(key).nullOr { intValue() }

    fun getDate(key:String):Date? = getLong(key).nullOr { Date(this) }

}

public class BListHelper(val list:BList) {

    fun map<T:BType, R>(mapper:(T)->R):List<R> = list.map { mapper(it as T) }

    fun get(index:Int):BType = list[index]

    fun getBMap(index:Int):BMap = get(index) as BMap

    fun getMap(index:Int): BMapHelper = BMapHelper(getBMap(index))

    fun getBList(index:Int):BList = get(index) as BList

    fun getBBytes(index:Int):BBytes = get(index) as BBytes

    fun getBNumber(index:Int):BNumber = get(index) as BNumber

    fun getBytes(index:Int):ByteArray = getBBytes(index).value

    fun getString(index:Int):String = getBytes(index).asString()
    fun getBigInteger(index:Int):BigInteger = getBNumber(index).value

    fun getLong(index:Int):Long = getBigInteger(index).longValue()
    fun getInt(index:Int):Int = getBigInteger(index).intValue()

    fun getDate(index:Int):Date = Date(getLong(index))

}