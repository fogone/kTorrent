package ru.nobirds.torrent.bencode

import java.math.BigInteger
import java.util.Date

public class BListBuilder(val blist:BList = BList()) {

    public fun value(string:String) {
        value(string.toByteArray())
    }

    public fun value(bytes:ByteArray) {
        value(BBytes().set(bytes))
    }

    public fun value(number:BigInteger) {
        value(BNumber().set(number))
    }

    public fun value(number:Long) {
        value(BigInteger(number.toString()))
    }

    public fun value(number:Int) {
        value(number.toLong())
    }

    public fun value(value:BValueType<out Any>) {
        blist.add(value)
    }

    public fun clear() {
        blist.clear()
    }

    public fun map(builder:BMapBuilder.()->Unit) {
        val result = BMapBuilder()
        result.builder()
        blist.add(result.build())
    }

    public fun list(builder: BListBuilder.()->Unit) {
        val result = BListBuilder()
        result.builder()
        blist.add(result.build())
    }

    public fun build():BList = blist

}

public class BMapBuilder(val bmap:BMap = BMap()) {

    public fun numberValue<T:Any>(name:String, number:T?, cast:(T)->BNumber) {
        if(number != null)
            value(name, cast(number))
    }

    public fun value(name:String, date:Date?) {
        numberValue(name, date) { BNumber().set(BigInteger(it.time.toString())) }
    }

    public fun value(name:String, number:Long?) {
        numberValue(name, number) { BNumber().set(BigInteger(it.toString())) }
    }

    public fun value(name:String, number:BigInteger?) {
        numberValue(name, number) { BNumber().set(it) }
    }

    public fun bytesValue<T:Any>(name:String, bytes:T?, cast:(T)->BBytes) {
        if(bytes != null)
            value(name, cast(bytes))
    }

    public fun value(name:String, string:String?) {
        bytesValue(name, string) { BBytes().set(it.toByteArray()) }
    }

    public fun value(name:String, bytes:ByteArray?) {
        bytesValue(name, bytes) { BBytes().set(it) }
    }

    public fun value(name:String, value:BValueType<out Any>) {
        bmap.putValue(name, value)
    }

    public fun remove(name:String) {
        bmap.remove(name)
    }

    public fun clear() {
        bmap.clear()
    }

    public fun map(name:String, builder:BMapBuilder.()->Unit) {
        BMapBuilder(bmap.getOrPutValue(name) { BMap() } as BMap).builder()
    }

    public fun map(name:String, map:BMap) {
        bmap.putValue(name, map)
    }

    public fun list(name:String, builder: BListBuilder.()->Unit) {
        BListBuilder(bmap.getOrPutValue(name) { BList() } as BList).builder()
    }

    public fun build():BMap = bmap

}

public object BTypeFactory {

    public fun createBMap(bmap:BMap = BMap(), builder:BMapBuilder.()->Unit):BMap {
        val result = BMapBuilder(bmap)
        result.builder()
        return result.build()
    }

}