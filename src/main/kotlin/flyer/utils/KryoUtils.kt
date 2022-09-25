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
        val kryo = kryoPool.obtain()
        try {
            Output(1024).use { opt ->
                kryo.writeObject(opt, obj)
                opt.flush()
                return opt.toBytes()
            }
        } finally {
            kryoPool.free(kryo)
        }
    }

    fun <T> deserialize(byteArray: ByteArray, clazz: Class<T>): T {
        val kryo = kryoPool.obtain()
        try {
            Input(byteArray).use { opt ->
                return kryo.readObject(opt, clazz) as T
            }
        } finally {
            kryoPool.free(kryo)
        }
    }
}


fun main() {
    val byteArray =  KryoUtils.serialize(1)
    val t = KryoUtils.deserialize(byteArray, Int::class.java)
    println(t)
}

