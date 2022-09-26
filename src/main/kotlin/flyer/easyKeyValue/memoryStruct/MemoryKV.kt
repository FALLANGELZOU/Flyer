package flyer.easyKeyValue.memoryStruct

import flyer.easyKeyValue.node.MemoryNode
import flyer.easyKeyValue.physicalFile.Chunk
import java.io.File

class MemoryKV(name: String){
    private lateinit var chunk: Chunk
    private val mMap = mutableMapOf<String, MemoryNode>()
    init {
        val dir = File("./EasyKV")
        if (!dir.exists() && !dir.isDirectory) dir.mkdir()
        chunk = Chunk(name)
    }

    fun load() {

    }

    fun flush() {

    }





}