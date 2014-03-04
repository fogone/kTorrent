package ru.nobirds.torrent.client.task

import ru.nobirds.torrent.client.task.file.CompositeRandomAccessFile

public class CompositeFileDescriptor(val files:List<FileDescriptor>) {

    val compositeRandomAccessFile = CompositeRandomAccessFile(files.map { it.randomAccessFile })

    public val length:Long = compositeRandomAccessFile.length


}