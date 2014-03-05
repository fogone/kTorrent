package ru.nobirds.torrent.config

public object Configs {

    public fun fromProperties(resource:String):Config {
        val properties = ClassLoader
                .getSystemResourceAsStream(resource)!!
                .use { stream ->
            val properties = java.util.Properties()
            properties.load(stream)
            properties
        }

        return Config(properties as Map<String, String>)
    }

}