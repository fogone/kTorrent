package ru.nobirds.torrent.client.task.state

public interface StateListener {

    fun onPieceComplete(piece:Int)

    fun onBlockComplete(piece:Int, block:Int)

}