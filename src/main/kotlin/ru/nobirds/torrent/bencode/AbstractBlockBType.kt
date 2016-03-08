package ru.nobirds.torrent.bencode

abstract class AbstractBlockBType(val start:Char, val end:Char = 'e') : AbstractBType() {

    private var started = false

    override var startPosition = -1L
    override var endPosition = -1L

    override fun processChar(stream: BTokenStream): Boolean {
        when(stream.currentChar()) {
            start -> {
                if(!started) {
                    startPosition = stream.position()
                    onStart(stream)
                    started = true
                }
                else
                    onChar(stream)
                    
                return false
            }
            end -> {
                endPosition = stream.position()+1
                onEnd(stream)
                return true
            }
            else -> {
                onChar(stream)
                return false
            }
        }
    }

    open fun onStart(stream: BTokenStream) {
    }

    open fun onEnd(stream: BTokenStream) {
    }

    abstract fun onChar(stream: BTokenStream)

}