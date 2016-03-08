package ru.nobirds.torrent.dht

class ErrorAnswerException(code:Int, message:String) : DhtException("error [$code] $message")