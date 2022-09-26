package flyer.easyKeyValue.physicalFile

import flyer.easyKeyValue.Config
import flyer.easyKeyValue.Config.GB
import flyer.easyKeyValue.Config.MB
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PhysicalDataHandler(kvName: String, val size: Long) {
    private val file = RandomAccessFile("$kvName.ekv", "rw")
    private val fileChannel = file.channel
    private var mappedByteBuffer: MappedByteBuffer
    var pos = Config.INT_SIZE
        set(value) {
            field = value
            mappedByteBuffer.putInt(0, value)
        }
    val ptr: MappedByteBuffer get() = mappedByteBuffer.position(pos)

    fun expand(expandSize: Long) {
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, pos.toLong(), file.length() + expandSize)
    }


    /**
     * 文件前四个字节为文件大小，int存储
     */
    private fun initLen() {
        pos = mappedByteBuffer.getInt(0)
        if (pos == 0) {
            mappedByteBuffer.putInt(0, Config.INT_SIZE)
            pos = Config.INT_SIZE
        }
    }

    /**
     * 获取实际写入多少字节
     */
    fun getLogicSize(): Int {
        return mappedByteBuffer.getInt(0)
    }

    /**
     * 获取文件映射大小
     */
    fun getPhysicalSize(): Long {
        return file.length()
    }

    init {
        mappedByteBuffer = if (file.length() == 0L) {
            fileChannel.map(FileChannel.MapMode.READ_WRITE, Config.INT_SIZE.toLong(), size)
        } else {
            fileChannel.map(FileChannel.MapMode.READ_WRITE, Config.INT_SIZE.toLong(), file.length())
        }
        initLen()
    }
}


