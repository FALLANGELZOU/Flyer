package test

import flyer.easyKeyValue.node.NodeFactory
import java.io.RandomAccessFile
import java.nio.channels.FileChannel

class t3 {
}

fun main() {
    NodeFactory.createNode("1")
    val fileChannel = RandomAccessFile("kv.data", "rw").channel
    val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1024)
    println(mappedByteBuffer.get(2))

}