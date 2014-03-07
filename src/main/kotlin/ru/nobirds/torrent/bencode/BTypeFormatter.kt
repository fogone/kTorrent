package ru.nobirds.torrent.bencode

import java.io.Writer
import ru.nobirds.torrent.asString
import ru.nobirds.torrent.toHexString
import ru.nobirds.torrent.containsNonPrintable

public class BTypeFormatter(val writer:Writer) {

    private var level = 0

    public fun format(value:BType) {
        when(value) {
            is BMap -> formatBMap(value)
            is BList -> formatBList(value)
            is BNumber -> formatBNumber(value)
            is BBytes -> formatBBytes(value)
        }
    }

    public fun formatBMap(bmap:BMap) {
        writer.write("{\n")

        level++
        for (pair in bmap.values()) {
            writeTabs()
            writer.write("${pair.name}:")
            format(pair.value)
            writer.write(",\n")
        }
        level--

        writeTabs()
        writer.write("}")
    }

    public fun formatBList(blist:BList) {
        writer.write("[\n")

        level++
        for (item in blist) {
            writeTabs()
            format(item)
            writer.write(",\n")
        }
        level--

        writeTabs()
        writer.write("]")
    }

    private fun writeTabs() {
        (level*2).times { writer.write(' '.toInt()) }
    }

    public fun formatBNumber(bnumber:BNumber) {
        writer.write("${bnumber.value}")
    }

    public fun formatBBytes(bbytes:BBytes) {

        val string = bbytes.value.asString()

        if(string.containsNonPrintable())
            writer.write("${bbytes.value.toHexString()}")
        else
            writer.write("'${string}'")
    }
}