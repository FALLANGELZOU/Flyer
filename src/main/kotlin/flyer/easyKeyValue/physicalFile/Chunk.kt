package flyer.easyKeyValue.physicalFile

import flyer.easyKeyValue.Config
import flyer.easyKeyValue.KVUtil.Bytes.toAsciiString
import flyer.easyKeyValue.node.MemoryNode
import java.io.File

class Chunk(kvName: String, chunkSize: Long = Config.CHUNK_DEFAULT_SIZE) {
    private val mMap = mutableMapOf<String, MemoryNode>()
    private val handler = PhysicalDataHandler("./EasyKV/${kvName}_chunk", chunkSize)
    private val stream = Stream(kvName)

    object StoreType{
        const val BOOL = 0
        const val INT = 1
    }

    /**
     * 4 byte
     *  +--------+------------------------+
     *  | tag    | index of key           |
     *  +--------+------------------------+
     *  0：是否有效
     *  1: 是否扩展
     *  2: bool值，或者是否是压缩的short/byte/char,如果是，占用剩下24位中的高位
     *  3: 预留
     *  4、5、6、7: 表示value的数据类型是bool/int/...，4bit可以表示16种类型
     *  类型表示:
     *  0: bool
     *  1: int
     *
     */
    private fun generatePhysicalKey(key: String, type: Int, boolValue: Boolean = false): Int {
        var raw = 0b10000000
        when(type) {
            StoreType.BOOL -> {
                raw = raw or if (boolValue)  0b00100000 else 0b00000000

            }
            StoreType.INT -> {
                raw = raw or 0b00000001
            }
        }

        val keyBytes = key.toByteArray()
        return raw shl 24 or stream.writeBytes(keyBytes.size, keyBytes)
    }

    /**
     * 8 byte
     *  +--------+------------------------+--------------------------------+
     *  | tag    | index of key           | value of int                   |
     *  +--------+------------------------+--------------------------------+
     */
    fun writeInt(key: String, value: Int) {
        val physicalKey = generatePhysicalKey(key, 1)
        handler.ptr.putInt(physicalKey)
        handler.pos += Config.INT_SIZE
        handler.ptr.putInt(value)
        handler.pos += Config.INT_SIZE
    }

    /**
     * 4 byte
     *  +--------+------------------------+
     *  | tag    | index of key           |
     *  +--------+------------------------+
     */
    fun writeBool(key: String, value: Boolean) {
        val physicalKey = generatePhysicalKey(key, 0, value)
        handler.ptr.putInt(physicalKey)
        handler.pos += Config.INT_SIZE
    }


    /**
     * 有效位验证
     */
    fun isValid(physicalKey: Int): Boolean {
        return (physicalKey shr 31 and 0x1) == 1
    }

    private fun getStoreType(physicalKey: Int): Int {
        return physicalKey shr 24 and 0x0F
    }

    fun loadAll(){
        var p = 4
        val size = handler.getLogicSize()
        while (p < size) {
            val position = p
            val physicalKey = handler.ptr.getInt(p)
            p += Config.INT_SIZE
            val keyIndex = physicalKey and 0xFFFFFF
            val keyName = stream.readBytes(keyIndex).toAsciiString()

            when(getStoreType(physicalKey)) {
                StoreType.BOOL -> {
                    mMap[keyName] = MemoryNode((physicalKey shr 29 and 0x1) == 1, position, StoreType.BOOL)
                }
                StoreType.INT -> {
                    mMap[keyName] = MemoryNode(handler.ptr.getInt(p), position, StoreType.INT)
                    p += Config.INT_SIZE
                }
            }
        }
        for(item in mMap) {
            println("${item.key} : ${item.value}")
        }
    }

}
