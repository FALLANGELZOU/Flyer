package flyer.easyKeyValue.physicalFile

import flyer.easyKeyValue.Config
import flyer.easyKeyValue.KVUtil.Bytes.toAsciiString
import flyer.easyKeyValue.node.MemoryNode

/**
 * 流文件
 *
 */
class Stream(kvName: String) {
    private val handler = PhysicalDataHandler("${kvName}_stream", Config.STREAM_DEFAULT_SIZE)

    /**
     * 写入字节，并返还首地址
     */
    fun writeBytes(len: Int, byteArray: ByteArray): Int {
        var totalBytes = getBlockNum(len) * Config.BLOCK_SIZE
        val currentPos = handler.pos
        handler.ptr.putInt(len)
        handler.pos += Config.INT_SIZE
        handler.ptr.put(byteArray)
        handler.pos = currentPos + totalBytes
        return currentPos
    }

    /**
     * 读取字节
     */
    fun readBytes(position: Int): ByteArray {
        var p = position
        val size = handler.ptr.getInt(p)
        p += Config.INT_SIZE
        val bytes = ByteArray(size)
        for (i in 0 until size) {
            bytes[i] = handler.ptr.get(p)
            p++
        }
        return bytes
    }

    /**
     * 传入len，计算改记录占用了多少个block
     */
    private fun getBlockNum(len: Int): Int {
        val totalBytes = len + Config.INT_SIZE
        val blockNum = totalBytes / Config.BLOCK_SIZE
        return if (totalBytes % Config.BLOCK_SIZE != 0) blockNum + 1 else blockNum
    }



    fun readAll() {
        val logicSize = handler.getLogicSize()
        println("占用字节：$logicSize")
        var p = 4
        while (p < logicSize) {
            val size = handler.ptr.getInt(p)
            var tmp = p
            tmp += 4
            val bytes = ByteArray(size)
            for (i in 0 until size) {
                bytes[i] = handler.ptr.get(tmp)
                tmp++
            }
            p += getBlockNum(size)*24
            val a = bytes.toAsciiString()
            println(a)
        }
    }
}

fun main() {
    val a = Stream("test")
    var str = "name is Efficient. MMKV uses mmap to keep memory synced with files, and protobuf to encode/decode values, making the most of Android to achieve the best performance.".toByteArray()
    a.writeBytes(str.size, str)
    str = "name".toByteArray()
    a.writeBytes(str.size, str)
    str = "making the most of Android to achieve the best performance.".toByteArray()
    a.writeBytes(str.size, str)
    a.readAll()
}