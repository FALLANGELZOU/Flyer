package flyer.easyKeyValue.memoryStruct

import flyer.easyKeyValue.EasyKV
import flyer.easyKeyValue.node.MemoryNode
import flyer.easyKeyValue.physicalFile.Chunk
import java.io.File
import java.util.concurrent.locks.Lock

import java.util.concurrent.locks.ReentrantReadWriteLock




class MemoryKV(name: String): EasyKV {
    private var chunk: Chunk
    private val mMap = mutableMapOf<String, MemoryNode>()
    private val rwl = ReentrantReadWriteLock()
    private val r: Lock = rwl.readLock()
    private val w: Lock = rwl.writeLock()

    init {
        val dir = File("./EasyKV")
        if (!dir.exists() && !dir.isDirectory) dir.mkdir()
        chunk = Chunk(name)
        chunk.loadAllFromFile().apply(mMap::putAll)
    }

    override operator fun get(key: String): Any? {
        r.lock()
        try {
            return mMap[key]?.value
        } finally {
            r.unlock()
        }
    }

    override operator fun <T> set(key: String, value: T) {
        w.lock()
        try {
            when(value) {
                is Int -> {
                    mMap[key] = chunk.writeInt(key, value)
                }
                else -> {
                    mMap[key] = chunk.writeAny(key, value)
                }
            }
        } finally {
            w.unlock()
        }
    }

    override fun remove(key: String) {
        w.lock()
        try {
            mMap[key]?.let {
                chunk.logicalRemove(it.position)
                mMap.remove(key)
            }
        } finally {
            w.unlock()
        }

    }


    /**
     * 加载所有持久化数据
     */
    fun reload() {
        mMap.clear()
        chunk.loadAllFromFile().apply(mMap::putAll)
    }

    /**
     * 全量刷新，整理文件
     */
    fun flush() {

    }

    fun getFileSize(): Long {
        return chunk.getChunkAndStreamByteSize()
    }


}


fun main() {
    val kv = MemoryKV("data")
    println(kv["key10"])
//    val start = System.currentTimeMillis()
    for(i in 0 .. 100000) {
        //kv["key$i"] = "this is value$i"
        println(kv["key$i"])
    }
}
