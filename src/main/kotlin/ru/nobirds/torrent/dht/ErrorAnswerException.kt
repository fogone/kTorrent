package ru.nobirds.torrent.dht

public class ErrorAnswerException(code:Int, message:String) : DhtException("error [$code] $message")