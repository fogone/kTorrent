package ru.nobirds.torrent.bencode

public abstract class AbstractBType<T> : BType<T> {

    public abstract fun processChar(stream: BTokenInputStream):Boolean

    public override fun process(stream: BTokenInputStream) {
        if(processByte(stream))
            return

        while(stream.hasNext()) {
            stream.next()
            if(processByte(stream))
                return
        }
    }

    private fun processByte(stream: BTokenInputStream):Boolean {
        if(stream.current() < 0)
            throw IllegalStateException()

        return processChar(stream)
    }
}