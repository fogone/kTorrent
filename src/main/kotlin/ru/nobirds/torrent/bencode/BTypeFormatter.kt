package ru.nobirds.torrent.bencode

import ru.nobirds.torrent.utils.asString
import ru.nobirds.torrent.utils.containsNonPrintable
import ru.nobirds.torrent.utils.forEachWithStatus
import java.io.Writer

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

        bmap.values().forEachWithStatus {
            writeTabs()
            val pair = it.value()
            writer.write("'${pair.name}':")
            format(pair.value)
            if(it.hasNext()) writer.write(",")
            writer.write("\n")
        }
        level--

        writeTabs()
        writer.write("}")
    }

    public fun formatBList(blist:BList) {
        writer.write("[\n")

        level++
        blist.forEachWithStatus {
            writeTabs()
            format(it.value())
            if(it.hasNext()) writer.write(",")
            writer.write("\n")
        }
        level--

        writeTabs()
        writer.write("]")
    }

    private fun writeTabs() {
        repeat(level*2) { writer.write(' '.toInt()) }
    }

    public fun formatBNumber(bnumber:BNumber) {
        writer.write("${bnumber.value}")
    }

    public fun formatBBytes(bbytes:BBytes) {

        val string = bbytes.value.asString()

        if(string.containsNonPrintable())
            formatBytes(bbytes.value)
        else
            writer.write("'${string}'")
    }

    private fun formatBytes(bytes:ByteArray) {
        writer.write("[")
        level++
        bytes.iterator().forEachWithStatus {
            if(it.index()%50 == 0) {
                writer.write("\n")
                writeTabs()
            }

            writer.write((it.value().toInt() and 0xFF).toString())
            if(it.hasNext())
                writer.write(",")
        }
        level--
        writer.write("]")
    }
}