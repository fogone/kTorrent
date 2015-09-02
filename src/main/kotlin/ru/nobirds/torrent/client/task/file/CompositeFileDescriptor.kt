package ru.nobirds.torrent.client.task.file

public class CompositeFileDescriptor(val files:List<FileDescriptor>) {

    val compositeRandomAccessFile = CompositeRandomAccessFile(files.map { it.randomAccessFile })

    public val length:Long = compositeRandomAccessFile.length


}