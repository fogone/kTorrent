package ru.nobirds.torrent.bencode

public abstract class AbstractBlockBType<T>(val start:Char, val end:Char = 'e') : AbstractBType<T>() {

    private var started = false

    private var valueImpl:T? = null

    override val value: T
        get() = valueImpl!!

    override fun processChar(stream: BTokenInputStream): Boolean {
        when(stream.currentChar()) {
            start -> {
                if(!started) {
                    onStart(stream)
                    started = true
                }
                else
                    onChar(stream)
                    
                return false
            }
            end -> {
                onEnd(stream)
                valueImpl = createResult()
                return true
            }
            else -> {
                onChar(stream)
                return false
            }
        }
    }

    public open fun onStart(stream: BTokenInputStream) {
    }

    public open fun onEnd(stream: BTokenInputStream) {
    }

    public abstract fun onChar(stream: BTokenInputStream)

    public abstract fun createResult():T

}