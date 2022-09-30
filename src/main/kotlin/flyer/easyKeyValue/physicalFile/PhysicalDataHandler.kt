package flyer.easyKeyValue.physicalFile

import flyer.easyKeyValue.KVConf
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PhysicalDataHandler(private val kvName: String, val size: Long) {
    private var mmap = Mmap(kvName, size)
    private var expandCnt = 0
    private val ptr: MappedByteBuffer get() = mmap.mappedByteBuffer.position(pos)
    val writtenBytes: Int get() = mmap.mappedByteBuffer.getInt(0)
    val fileSize: Long get() = mmap.fileSize
    var pos = Int.SIZE_BYTES
        set(value) {
            field = value
            mmap.mappedByteBuffer.putInt(0, value)
        }

    private fun expand() {
        expandCnt ++
        mmap = Mmap(kvName, mmap.size + expandCnt * KVConf.EXPAND_BLOCK_SIZE)
    }

    fun writeBytes(byteArray: ByteArray, position: Int? = null): Int {
        while (byteArray.size + (position ?: pos) >= mmap.size) expand()
        return if (position != null) {
            mmap.mappedByteBuffer.put(position, byteArray)
            position + byteArray.size
        } else {
            ptr.put(byteArray)
            pos += byteArray.size
            pos
        }
    }

    fun readBytes(position: Int, size: Int): ByteArray {
        while (size + position >= mmap.size) expand()
        val bytes = ByteArray(size)
        for (i in 0 until size) {
            try {
                bytes[i] = mmap.mappedByteBuffer.get(position + i)
            }catch (e: Exception){
                println(e)
            }

        }
        return bytes
    }

    fun writeInt(value: Int, position: Int? = null): Int {
        while (Int.SIZE_BYTES + (position ?: pos) >= mmap.size) expand()
        return if (position != null) {
            mmap.mappedByteBuffer.putInt(position, value)
            position + Int.SIZE_BYTES
        } else {
            ptr.putInt(value)
            pos += Int.SIZE_BYTES
            pos
        }
    }

    fun readInt(position: Int): Int {
        while (Int.SIZE_BYTES + position >= mmap.size) expand()
        return mmap.mappedByteBuffer.getInt(position)
    }




    init {
        pos = mmap.mappedByteBuffer.getInt(0)
        if (pos == 0) {
            mmap.mappedByteBuffer.putInt(0, Int.SIZE_BYTES)
            pos = Int.SIZE_BYTES
        } else if(mmap.size <= pos) {
            mmap = Mmap(kvName, pos.toLong())
        }
    }

    inner class Mmap(kvName: String, val size: Long) {
        private var file = RandomAccessFile("$kvName", "rw")
        private var fileChannel = file.channel
        var mappedByteBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, KVConf.INT_SIZE.toLong(), size)
        var fileSize = 0L
        init {
            fileSize = file.length()
            fileChannel.close()
            file.close()

        }
    }
}


