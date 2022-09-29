package flyer.easyKeyValue.physicalFile

import flyer.easyKeyValue.Config
import flyer.easyKeyValue.KVUtil.Bytes.toAsciiString
import flyer.easyKeyValue.node.MemoryNode
import flyer.utils.KryoUtils
import java.io.File
import java.lang.Exception

class Chunk(private val kvName: String, chunkSize: Long = Config.CHUNK_DEFAULT_SIZE) {
    private var handler = PhysicalDataHandler("./EasyKV/${kvName}_chunk", chunkSize)
    private val stream = Stream(kvName)

    object StoreType{
        const val BOOL = 0
        const val INT = 1
        const val STRING = 2
        const val ANY = 3
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
            StoreType.STRING -> {
                raw = raw or 0b00000010
            }
            StoreType.ANY -> {
                raw = raw or 0b00000011
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
    fun writeInt(key: String, value: Int): MemoryNode {
        val physicalKey = generatePhysicalKey(key, StoreType.INT)
        val position = handler.pos
        handler.ptr.putInt(physicalKey)
        handler.pos += Config.INT_SIZE
        handler.ptr.putInt(value)
        handler.pos += Config.INT_SIZE
        return MemoryNode(value, position, StoreType.INT)
    }

    fun reWriterInt(key: String, value: Int, position: Int): MemoryNode {
        if (getLogicalKey(position) == key) {
            handler.ptr.putInt(position + Int.SIZE_BYTES, value)
            return MemoryNode(value, position, StoreType.INT)
        } else {
            // TODO: use unique exception
            throw Exception("")
        }
    }

    /**
     * 4 byte
     *  +--------+------------------------+
     *  | tag    | index of key           |
     *  +--------+------------------------+
     */
    fun writeBool(key: String, value: Boolean) {
        val physicalKey = generatePhysicalKey(key, StoreType.BOOL, value)
        handler.ptr.putInt(physicalKey)
        handler.pos += Config.INT_SIZE
    }


    /**
     * 8 byte
     *  +--------+------------------------+--------------------------------+
     *  | tag    | index of key           | index of string in stream      |
     *  +--------+------------------------+--------------------------------+
     */
    fun writeString(key: String, value: String) {
        val physicalKey = generatePhysicalKey(key, StoreType.STRING)
        handler.ptr.putInt(physicalKey)
        handler.pos += Config.INT_SIZE
        val bytes = value.toByteArray()
        val index = stream.writeBytes(bytes.size, bytes)
        handler.ptr.putInt(index)
        handler.pos += Config.INT_SIZE
    }

    /**
     * 8 byte
     *  +--------+------------------------+--------------------------------+
     *  | tag    | index of key           | index of any in stream         |
     *  +--------+------------------------+--------------------------------+
     */
    fun <T> writeAny(key: String, value: T): MemoryNode {
        if (handler.pos + 2 * Int.SIZE_BYTES >= handler.size) handler = PhysicalDataHandler("./EasyKV/${kvName}_chunk", handler.pos + Config.STREAM_DEFAULT_SIZE)
        val physicalKey = generatePhysicalKey(key, StoreType.ANY)
        val position = handler.pos
        handler.ptr.putInt(physicalKey)
        handler.pos += Config.INT_SIZE
        val bytes = KryoUtils.serialize(value)
        val index = stream.writeBytes(bytes.size, bytes)
        handler.ptr.putInt(index)
        handler.pos += Config.INT_SIZE
        return MemoryNode(value as Any, position, StoreType.ANY)
    }

    fun <T> reWriteAny(key: String, value: T, position: Int): MemoryNode {
        if (getLogicalKey(position) == key) {
            val bytes = KryoUtils.serialize(value)
            val index = stream.writeBytes(bytes.size, bytes)
            handler.ptr.putInt(position + Int.SIZE_BYTES, index)
            return MemoryNode(value as Any, position, StoreType.ANY)
        } else {
            // TODO: use unique exception
            throw Exception("")
        }
    }


    /**
     * 有效位验证
     */
    fun isValid(physicalKey: Int): Boolean {
        return (physicalKey shr 31 and 0x1) == 1
    }

    /**
     * 从key中获取数据类型
     */
    private fun getStoreType(physicalKey: Int): Int {
        return physicalKey shr 24 and 0x0F
    }

    /**
     * 逻辑删除
     */
    fun logicalRemove(position: Int) {
        val physicalKey = handler.ptr.getInt(position)
        handler.ptr.putInt(position, physicalKey and 0x7FFFFFFF)
    }

    /**
     * 获取键的名称
     */
    fun getLogicalKey(position: Int): String {
        val physicalKey = handler.ptr.getInt(position)
        val keyIndex = physicalKey and 0xFFFFFF
        return stream.readBytes(keyIndex).toAsciiString()
    }

    fun loadAllFromFile(): MutableMap<String, MemoryNode> {
        val mMap = mutableMapOf<String, MemoryNode>()
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
                StoreType.STRING -> {
                    val index = handler.ptr.getInt(p)
                    val bytes = stream.readBytes(index)
                    val str = bytes.toAsciiString()
                    mMap[keyName] = MemoryNode(str, position, StoreType.STRING)
                    p += Config.INT_SIZE
                }
                StoreType.ANY -> {
                    val index = handler.ptr.getInt(p)
                    val bytes = stream.readBytes(index)
                    val any = KryoUtils.deserialize(bytes)
                    mMap[keyName] = MemoryNode(any, position, StoreType.ANY)
                    p += Config.INT_SIZE
                }
            }
        }
        return mMap
    }

}
