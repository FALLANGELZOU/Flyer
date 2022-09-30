package flyer.easyKeyValue.physicalFile

import flyer.easyKeyValue.KVConf
import flyer.easyKeyValue.KVConf.isNotNullOrOther
import flyer.easyKeyValue.KVUtil.Bytes.toAsciiString
import flyer.easyKeyValue.node.MemoryNode
import flyer.utils.KryoUtils
import java.lang.Exception

class Chunk(private val kvName: String, chunkSize: Long = KVConf.CHUNK_DEFAULT_SIZE) {
    private var handler = PhysicalDataHandler("./EasyKV/${kvName}.ckv", chunkSize)
    private val stream = Stream(kvName)
    private val pos get() = handler.pos

    val writtenBytes get() = handler.writtenBytes

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
        val currentPos = stream.pos
        stream.writeBytes(keyBytes.size, keyBytes)
        return raw shl 24 or currentPos
    }

    /**
     * 8 byte
     *  +--------+------------------------+--------------------------------+
     *  | tag    | index of key           | index of any in stream         |
     *  +--------+------------------------+--------------------------------+
     */
    fun <T> writeAny(key: String, value: T, position: Int? = null): MemoryNode {
        val currentPos = pos
        val physicalKey = generatePhysicalKey(key, StoreType.ANY)
        handler.writeInt(physicalKey, position)
        handler.writeInt(stream.pos, if (position != null) Int.SIZE_BYTES + position else null)
        val streamBytes = KryoUtils.serialize(value)
        stream.writeBytes(streamBytes.size, streamBytes)
        return MemoryNode(value as Any, position ?: currentPos, StoreType.ANY)
    }


    /**
     * 8 byte
     *  +--------+------------------------+--------------------------------+
     *  | tag    | index of key           | value of int                   |
     *  +--------+------------------------+--------------------------------+
     */
    fun writeInt(key: String, value: Int, position: Int? = null): MemoryNode {
        val currentPos = pos
        val physicalKey = generatePhysicalKey(key, StoreType.INT)
        handler.writeInt(physicalKey, position)
        handler.writeInt(value, if (position != null) Int.SIZE_BYTES + position else null)
        return MemoryNode(value, position ?: currentPos, StoreType.INT)
    }


    /**
     * 4 byte
     *  +--------+------------------------+
     *  | tag    | index of key           |
     *  +--------+------------------------+
     */
    fun writeBool(key: String, value: Boolean) {
    }


    /**
     * 8 byte
     *  +--------+------------------------+--------------------------------+
     *  | tag    | index of key           | index of string in stream      |
     *  +--------+------------------------+--------------------------------+
     */
    fun writeString(key: String, value: String) {
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
        val physicalKey = handler.readInt(position)
        handler.writeInt(physicalKey and 0x7FFFFFFF, position)
    }

    /**
     * 获取键的名称
     */
    fun getLogicalKey(position: Int): String {
        val physicalKey = handler.readInt(position)
        val keyIndex = physicalKey and 0xFFFFFF
        return stream.readBytes(keyIndex).toAsciiString()
    }

    fun getChunkAndStreamByteSize(): Long {
        return writtenBytes.toLong() + stream.writtenBytes.toLong()
    }
    fun loadAllFromFile(): MutableMap<String, MemoryNode> {
        val mMap = mutableMapOf<String, MemoryNode>()
        var p = Int.SIZE_BYTES
        val size = handler.writtenBytes
        while (p < size) {
            val position = p
            val physicalKey = handler.readInt(p)
            p += KVConf.INT_SIZE
            if (!isValid(physicalKey)) {
                p += KVConf.INT_SIZE
                continue
            }
            val keyIndex = physicalKey and 0xFFFFFF
            var keyName = ""
            try {
                keyName = stream.readBytes(keyIndex).toAsciiString()
            }catch (e: Exception) {
                println(e)
            }

            when(getStoreType(physicalKey)) {
                StoreType.BOOL -> {
                    mMap[keyName] = MemoryNode((physicalKey shr 29 and 0x1) == 1, position, StoreType.BOOL)
                }
                StoreType.INT -> {
                    mMap[keyName] = MemoryNode(handler.readInt(p), position, StoreType.INT)
                    p += KVConf.INT_SIZE
                }
                StoreType.STRING -> {
                    val index = handler.readInt(p)
                    val bytes = stream.readBytes(index)
                    val str = bytes.toAsciiString()
                    mMap[keyName] = MemoryNode(str, position, StoreType.STRING)
                    p += KVConf.INT_SIZE
                }
                StoreType.ANY -> {
                    val index = handler.readInt(p)
                    val bytes = stream.readBytes(index)
                    val any = KryoUtils.deserialize(bytes)
                    mMap[keyName] = MemoryNode(any, position, StoreType.ANY)
                    p += KVConf.INT_SIZE
                }
            }
        }
        return mMap
    }

}
