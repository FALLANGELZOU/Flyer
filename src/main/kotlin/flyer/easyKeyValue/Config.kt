package flyer.easyKeyValue


object Config {
    inline val Number.KB
        get() = this.toLong()*1024
    inline val Number.MB
        get() = this.KB*1024
    inline val Number.GB
        get() = this.MB*1024

    val CHUNK_DEFAULT_SIZE = 512.KB
    val STREAM_DEFAULT_SIZE = 2.MB

    const val BLOCK_SIZE = 24
    const val INT_SIZE = 4

}

