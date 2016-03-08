package ru.nobirds.torrent.client.task.file

class CompositeFileDescriptor(val files:List<FileDescriptor>) {

    val compositeRandomAccessFile = CompositeRandomAccessFile(files.map { it.randomAccessFile })

    val length:Long = compositeRandomAccessFile.length


}