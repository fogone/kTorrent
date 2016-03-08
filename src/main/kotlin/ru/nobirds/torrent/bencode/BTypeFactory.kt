package ru.nobirds.torrent.bencode

import java.math.BigInteger
import java.util.Date

class BListBuilder(val blist:BList = BList()) {

    fun value(string:String) {
        value(string.toByteArray())
    }

    fun value(bytes:ByteArray) {
        value(BBytes().set(bytes))
    }

    fun value(number:BigInteger) {
        value(BNumber().set(number))
    }

    fun value(number:Long) {
        value(BigInteger(number.toString()))
    }

    fun value(number:Int) {
        value(number.toLong())
    }

    fun value(value:BValueType<out Any>) {
        blist.add(value)
    }

    fun clear() {
        blist.clear()
    }

    fun map(builder:BMapBuilder.()->Unit) {
        val result = BMapBuilder()
        result.builder()
        blist.add(result.build())
    }

    fun list(builder: BListBuilder.()->Unit) {
        val result = BListBuilder()
        result.builder()
        blist.add(result.build())
    }

    fun build():BList = blist

}

class BMapBuilder(val bmap:BMap = BMap()) {

    fun <T:Any> numberValue(name:String, number:T?, cast:(T)->BNumber) {
        if(number != null)
            value(name, cast(number))
    }

    fun value(name:String, date:Date?) {
        numberValue(name, date) { BNumber().set(BigInteger(it.time.toString())) }
    }

    fun value(name:String, number:Long?) {
        numberValue(name, number) { BNumber().set(BigInteger(it.toString())) }
    }

    fun value(name:String, number:BigInteger?) {
        numberValue(name, number) { BNumber().set(it) }
    }

    fun <T:Any> bytesValue(name:String, bytes:T?, cast:(T)->BBytes) {
        if(bytes != null)
            value(name, cast(bytes))
    }

    fun value(name:String, string:String?) {
        bytesValue(name, string) { BBytes().set(it.toByteArray()) }
    }

    fun value(name:String, bytes:ByteArray?) {
        bytesValue(name, bytes) { BBytes().set(it) }
    }

    fun value(name:String, value:BValueType<out Any>) {
        bmap.putValue(name, value)
    }

    fun remove(name:String) {
        bmap.remove(name)
    }

    fun clear() {
        bmap.clear()
    }

    fun map(name:String, builder:BMapBuilder.()->Unit) {
        BMapBuilder(bmap.getOrPutValue(name) { BMap() } as BMap).builder()
    }

    fun map(name:String, map:BMap) {
        bmap.putValue(name, map)
    }

    fun list(name:String, builder: BListBuilder.()->Unit) {
        BListBuilder(bmap.getOrPutValue(name) { BList() } as BList).builder()
    }

    fun build():BMap = bmap

}

object BTypeFactory {

    fun createBMap(bmap:BMap = BMap(), builder:BMapBuilder.()->Unit):BMap {
        val result = BMapBuilder(bmap)
        result.builder()
        return result.build()
    }

}