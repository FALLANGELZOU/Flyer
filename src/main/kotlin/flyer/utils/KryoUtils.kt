package flyer.utils

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.util.Pool

object KryoUtils {
    private val kryoPool = object : Pool<Kryo>(true, false, 128) {
        override fun create(): Kryo {
            val kryo = Kryo()
            // 关闭序列化注册，会导致性能些许下降，但在分布式环境中，注册类生成ID不一致会导致错误
            kryo.isRegistrationRequired = false
            // 支持循环引用，也会导致性能些许下降 T_T
            kryo.references = true
            return kryo
        }
    }

    fun serialize(obj: Any): ByteArray {
        val kryo = kryoPool.obtain() // 使用 Output 对象池会导致序列化重复的错误（getBuffer返回了Output对象的buffer引用）
        try {
            Output(1024, -1).use { opt ->
                kryo.writeClassAndObject(opt, obj)
                val byteArray = opt.buffer
                opt.flush()
                opt.close()
                return byteArray
            }
        } finally {
            kryoPool.free(kryo)
        }
    }

    fun <T> deserialize(byteArray: ByteArray): T {
        val kryo = kryoPool.obtain()
        try {
            Input(1024).use { opt ->
                return kryo.readClassAndObject(opt) as T
            }
        } finally {
            kryoPool.free(kryo)
        }
    }
}

class a() {
    val name = "1"
    val age = 1
}
fun main() {
    val byteArray = KryoUtils.serialize(a())
    println(byteArray.toString())
    val b = KryoUtils.deserialize<a>(byteArray)
    println(b.toString())
}

