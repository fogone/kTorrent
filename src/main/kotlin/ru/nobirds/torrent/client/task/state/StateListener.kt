package ru.nobirds.torrent.client.task.state

interface StateListener {

    fun onPieceComplete(piece:Int)

    fun onBlockComplete(piece:Int, block:Int)

}