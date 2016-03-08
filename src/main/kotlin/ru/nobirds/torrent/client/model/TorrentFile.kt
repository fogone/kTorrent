package ru.nobirds.torrent.client.model

data class TorrentFile(
        val length:Long,
        val path:List<String>) {

    fun equals(other:TorrentFile):Boolean {
        if(!other.length.equals(length)) return false
        if(!other.path.equals(path)) return false

        return true
    }

}
