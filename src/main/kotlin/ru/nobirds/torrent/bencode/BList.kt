package ru.nobirds.torrent.bencode

import ru.nobirds.torrent.utils.asString
import java.math.BigInteger
import java.util.ArrayList
import java.util.Date

class BList(private val children:MutableList<BType> = ArrayList())
    : AbstractBlockBType('l'), MutableList<BType> by children {

    override fun onChar(stream: BTokenStream) {
        children.add(stream.processBType())
    }

    fun getBMap(index:Int):BMap = get(index) as BMap

    fun getBList(index:Int):BList = get(index) as BList

    fun getBBytes(index:Int):BBytes = get(index) as BBytes

    fun getBNumber(index:Int):BNumber = get(index) as BNumber

    fun getBytes(index:Int):ByteArray = getBBytes(index).value

    fun getString(index:Int):String = getBytes(index).asString()
    fun getBigInteger(index:Int):BigInteger = getBNumber(index).value

    fun getLong(index:Int):Long = getBigInteger(index).toLong()
    fun getInt(index:Int):Int = getBigInteger(index).toInt()

    fun getDate(index:Int):Date = Date(getLong(index))

}