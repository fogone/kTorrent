package ru.nobirds.torrent.config

import java.util.HashMap
import ru.nobirds.torrent.component1
import ru.nobirds.torrent.component2
import java.io.File

public class Config(val raw: Map<String, String>) {

    private val values = HashMap<ConfigProperty<out Any>, Any>()

    public fun get<V>(property: ConfigProperty<V>): V {
        val stored = values[property]

        if(stored != null)
            return stored as V

        val value = raw[property.name]

        if (value == null) {
            if (property.hasDefault)
                return property.default!!
            else
                throw IllegalArgumentException("No config value for name ${property.name}!")
        }

        val parsed = property.parser(value)

        values[property] = parsed

        return parsed
    }

}

public data class ConfigProperty<V>(val name: String, val default: V?, val parser: (String) -> V) {

    val hasDefault: Boolean = default != null

}

public object Parsers {

    public val stringParser:(String)->String = { it }

    public val longParser:(String)->Long = { it.toLong() }

    public val longRangeParser:(String)->LongRange = {
        val (start, end) = it.split("\\.\\.")
        start.toLong().rangeTo(end.toLong())
    }

    public val directoryParser:(String)->File = {
        val file = File(it)

        if(file.exists() && !file.isDirectory())
            throw IllegalStateException("File $it is not a directory")

        if(!file.mkdirs())
            throw IllegalArgumentException("Can't create directory $it")

        file
    }

}

public object Properties {

    public fun property<V>(name:String, default:V, parser:(String)->V):ConfigProperty<V> =
            ConfigProperty(name, default, parser)

    public fun property<V>(name:String, parser:(String)->V):ConfigProperty<V> =
            ConfigProperty(name, null, parser)

    public fun long(name:String):ConfigProperty<Long> = property(name, Parsers.longParser)
    public fun long(name:String, default:Long):ConfigProperty<Long> = property(name, default, Parsers.longParser)

    public fun longRange(name:String):ConfigProperty<LongRange> = property(name, Parsers.longRangeParser)
    public fun longRange(name:String, default:LongRange):ConfigProperty<LongRange> = property(name, default, Parsers.longRangeParser)

    public fun string(name:String):ConfigProperty<String> = property(name, Parsers.stringParser)
    public fun string(name:String, default:String):ConfigProperty<String> = property(name, default, Parsers.stringParser)

    public fun directory(name:String):ConfigProperty<File> = property(name, Parsers.directoryParser)
    public fun directory(name:String, default:File):ConfigProperty<File> = property(name, default, Parsers.directoryParser)
}