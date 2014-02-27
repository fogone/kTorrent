package ru.nobirds.torrent

fun <P, R> P?.nullOr(body:P.()->R):R?
        = if(this == null) null else body()

fun ByteArray.asString():String = String(this, "UTF-8")