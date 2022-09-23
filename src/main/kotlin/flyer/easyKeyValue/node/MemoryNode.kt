package flyer.easyKeyValue.node

<<<<<<< HEAD
data class MemoryNode (
    val value: Any,
    val position: Int,
    val type: Int
=======
data class MemoryNode<S> (
    val value: S,
    val position: Int,
    val byteSize: Int
>>>>>>> p/sunnan/flyer/test/work
    )