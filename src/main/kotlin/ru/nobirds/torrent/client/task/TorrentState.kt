package ru.nobirds.torrent.client.task

import java.util.BitSet

public class TorrentState(val size:Int, val state:BitSet = BitSet(size)) {

    public fun isDone(index:Int):Boolean = state.get(index)

    public fun done(index:Int) {
        state.set(index)
    }

}