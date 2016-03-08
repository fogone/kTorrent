package ru.nobirds.torrent.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class LinksCounter<T> {

    private val counters = ConcurrentHashMap<T, AtomicInteger>()

    fun increase(value:T):Int
            = counters.getOrPut(value) { AtomicInteger(0) }.incrementAndGet()

    fun decrease(value:T): Int
            = counters.getOrElse(value) { throw RuntimeException("No links") }.decrementAndGet()
}