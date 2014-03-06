package ru.nobirds.torrent.bencode

import java.math.BigInteger

public class BListBuilder(val blist:BList = BList()) {

    public fun value(string:String) {
        value(string.getBytes())
    }

    public fun value(bytes:ByteArray) {
        value(BBytes().set(bytes))
    }

    public fun value(number:BigInteger) {
        value(BNumber().set(number))
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

public class BMapBuilder(val bmap:BMap= BMap()) {

    public fun value(name:String, string:String) {
        value(name, string.getBytes())
    }

    public fun value(name:String, bytes:ByteArray) {
        value(name, BBytes().set(bytes))
    }

    public fun value(name:String, number:BigInteger) {
        value(name, BNumber().set(number))
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

    public fun list(name:String, builder: BListBuilder.()->Unit) {
        BListBuilder(bmap.getOrPutValue(name) { BList() } as BList).builder()
    }

    public fun build():BMap = bmap

}

public object BTypeFactory {

    public fun createBMap(bmap:BMap = BMap(), builder:BMapBuilder.()->Unit):BType {
        val result = BMapBuilder(bmap)
        result.builder()
        return result.build()
    }

}