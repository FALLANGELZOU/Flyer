package flyer.easyKeyValue

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


object KVConf {
    inline val Number.KB
        get() = this.toLong()*1024
    inline val Number.MB
        get() = this.KB*1024
    inline val Number.GB
        get() = this.MB*1024

    val CHUNK_DEFAULT_SIZE = 512.KB
    val STREAM_DEFAULT_SIZE = 2.MB
    val EXPAND_BLOCK_SIZE = 20.MB

    const val BLOCK_SIZE = 24
    const val INT_SIZE = 4
    const val LONG_SIZE = 8

    inline fun <T>  T.isNotNullOrOther(notNullBlock: (T & Any) -> Unit, nullBlock: () -> Unit) {
        if (this != null) {
            notNullBlock(this)
        } else {
            nullBlock()
        }
    }
}

