package flyer.easyKeyValue.physicalFile

import flyer.easyKeyValue.Config
import flyer.easyKeyValue.Config.GB
import flyer.easyKeyValue.Config.MB
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PhysicalDataHandler(private val kvName: String, val size: Long) {
    private var file = RandomAccessFile("$kvName.ekv", "rw")
    private var fileChannel = file.channel
    private var mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, Config.INT_SIZE.toLong(), size)

    var pos = Config.INT_SIZE
        set(value) {
            field = value
            mappedByteBuffer.putInt(0, value)
        }
    val ptr: MappedByteBuffer get() = mappedByteBuffer.position(pos)



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
        initLen()
        fileChannel.close()
        file.close()
    }
}


