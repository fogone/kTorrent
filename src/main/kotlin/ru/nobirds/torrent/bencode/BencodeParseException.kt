package ru.nobirds.torrent.bencode

open class BencodeParseException(message:String, cause: Throwable? = null) : RuntimeException(message, cause)

class IllegalCharacterException(val char: Char, position: Long) : BencodeParseException("Illegal character [$char] ${char.toInt()} at $position")
