package flyer.easyKeyValue.node

data class MemoryNode<S> (
    val value: S,
    val position: Int,
    val byteSize: Int
    )