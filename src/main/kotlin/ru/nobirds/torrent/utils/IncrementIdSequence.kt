package ru.nobirds.torrent.utils

import java.util.concurrent.atomic.AtomicLong

class IncrementIdSequence : IdSequence {

    private val counter = AtomicLong(1)

    override fun next(): String = counter.andIncrement.toString()

}