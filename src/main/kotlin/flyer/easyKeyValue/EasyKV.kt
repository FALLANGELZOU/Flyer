package flyer.easyKeyValue

interface EasyKV {
    operator fun get(key: String): Any?
    operator fun <T> set(key: String, value: T)
    fun remove(key: String)
}