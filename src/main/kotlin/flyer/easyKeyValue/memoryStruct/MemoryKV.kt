package flyer.easyKeyValue.memoryStruct

import flyer.easyKeyValue.Config
import flyer.easyKeyValue.KVUtil.Bytes.toAsciiString
import flyer.easyKeyValue.node.MemoryNode
import flyer.easyKeyValue.physicalFile.Chunk
import flyer.utils.KryoUtils
import java.io.File

class MemoryKV(name: String){
    private lateinit var chunk: Chunk
    private val mMap = mutableMapOf<String, MemoryNode>()
    init {
        val dir = File("./EasyKV")
        if (!dir.exists() && !dir.isDirectory) dir.mkdir()
        chunk = Chunk(name)
        chunk.loadAllFromFile().apply(mMap::putAll)
    }

    /**
     * 加载所有持久化数据
     */
    fun load() {
        mMap.clear()
        chunk.loadAllFromFile().apply(mMap::putAll)
    }

    /**
     * 全量刷新，整理文件
     */
    fun flush() {

    }

    operator fun get(key: String): Any? {
        return mMap[key]?.value
    }

    operator fun <T> set(key: String, value: T) {
        if (mMap.containsKey(key) && mMap[key] != null) {
            when (value) {
                is Int -> {
                    mMap[key]?.let {
                        mMap[key] = chunk.reWriterInt(key, value, it.position)
                    }
                }
                else -> {
                    mMap[key]?.let {
                        mMap[key] = chunk.reWriteAny(key, value, it.position)
                    }
                }
            }
        } else {
            when(value) {
                is Int -> {
                    mMap[key] = chunk.writeInt(key, value)
                }
                else -> {
                    mMap[key] = chunk.writeAny(key, value)
                }
            }
        }
    }
}

class Person() {
    var name: String? = null
    var age: Int? = null
    override fun toString(): String {
        return "name: $name, age: $age"
    }

}
fun main() {
    val kv = MemoryKV("test")
    val start = System.currentTimeMillis()
    for( i in 1..10000000) {
        kv["key$i"] = "this is value $i"
    }

}