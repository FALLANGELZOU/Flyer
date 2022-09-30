package flyer.easyKeyValue.physicalFile

import flyer.easyKeyValue.KVConf

/**
 * 流文件
 *
 */
class Stream(private val kvName: String) {
    private var handler = PhysicalDataHandler("./EasyKV/${kvName}.skv", KVConf.STREAM_DEFAULT_SIZE)
    val pos get() = handler.pos
    val writtenBytes get() = handler.writtenBytes
    /**
     * 写入字节
     */
    fun writeBytes(len: Int, byteArray: ByteArray): Int {
        var nextPos = handler.pos + getBlockNum(len) * KVConf.BLOCK_SIZE
        handler.writeInt(len)
        handler.writeBytes(byteArray)
        handler.pos = nextPos
        return nextPos
    }

    /**
     * 读取字节
     */
    fun readBytes(position: Int): ByteArray {
        val len = handler.readInt(position)
        return handler.readBytes(position + Int.SIZE_BYTES, len)
    }

    /**
     * 传入len，计算改记录占用了多少个block
     */
    private fun getBlockNum(len: Int): Int {
        val totalBytes = len + KVConf.INT_SIZE
        val blockNum = totalBytes / KVConf.BLOCK_SIZE
        return if (totalBytes % KVConf.BLOCK_SIZE != 0) blockNum + 1 else blockNum
    }
}