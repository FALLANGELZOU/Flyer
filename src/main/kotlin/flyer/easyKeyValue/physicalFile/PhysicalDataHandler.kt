package flyer.easyKeyValue.physicalFile

import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PhysicalDataHandler(kvName: String, val size: Long) {
    private val file = RandomAccessFile("$kvName.ekv", "rw")
    private val fileChannel = file.channel
    private var mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 4, size)
    var pos = 4
        set(value) {
            field = value
            mappedByteBuffer.putInt(0, value)
        }
    val ptr: MappedByteBuffer get() = mappedByteBuffer.position(pos)

    fun expand(expandSize: Long) {
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, pos.toLong(), size + expandSize)
    }


    /**
     * 文件前四个字节为文件大小，int存储
     */
    private fun initLen() {
        pos = mappedByteBuffer.getInt(0)
        if (pos == 0) {
            mappedByteBuffer.putInt(0, 4)
            pos = 4
        }
    }

    fun getLogicSize(): Int {
        return mappedByteBuffer.getInt(0)
    }

    init {
        initLen()
    }

}


