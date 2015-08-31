package ru.nobirds.torrent.bencode

public abstract class AbstractBType : BType {

    public abstract fun processChar(stream: BTokenStream):Boolean

    public override fun process(stream: BTokenStream) {
        if(processByte(stream))
            return

        while(stream.hasNext()) {
            stream.next()
            if(processByte(stream))
                return
        }
    }

    private fun processByte(stream: BTokenStream):Boolean {
        if(stream.current() < 0)
            throw IllegalStateException()

        return processChar(stream)
    }
}