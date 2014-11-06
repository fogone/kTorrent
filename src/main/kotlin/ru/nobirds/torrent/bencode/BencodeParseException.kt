package ru.nobirds.torrent.bencode

public open class BencodeParseException(message:String, cause: Throwable? = null) : RuntimeException(message, cause)

public class IllegalCharacterException(val char: Char) : BencodeParseException("Illegal character [${char}] ${char.toInt()}")
