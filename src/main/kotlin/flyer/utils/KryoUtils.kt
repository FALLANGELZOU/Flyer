package flyer.utils

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.util.Pool

object KryoUtils {
    private val kryoPool = object : Pool<Kryo>(true, false, 128) {
        override fun create(): Kryo {
            val kryo = Kryo()
            kryo.isRegistrationRequired = false
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





