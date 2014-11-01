package ru.nobirds.torrent.utils

import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

public class LinksCounter<T> {

    private val counters = ConcurrentHashMap<T, AtomicInteger>()

    public fun increase(value:T):Int
            = counters.getOrPut(value) { AtomicInteger(0) }.incrementAndGet()

    public fun decrease(value:T): Int
            = counters.getOrElse(value) { throw RuntimeException("No links") }.decrementAndGet()
}