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

    fun <T> serialize(obj: T): ByteArray {
        val kryo = kryoPool.obtain()
        try {
            Output(1024).use { opt ->
                kryo.writeClassAndObject(opt, obj)
                opt.flush()
                return opt.toBytes()
            }
        } finally {
            kryoPool.free(kryo)
        }
    }

    fun deserialize(byteArray: ByteArray): Any {
        val kryo = kryoPool.obtain()
        try {
            Input(byteArray).use { opt ->
                return kryo.readClassAndObject(opt)
            }
        } finally {
            kryoPool.free(kryo)
        }
    }
}





