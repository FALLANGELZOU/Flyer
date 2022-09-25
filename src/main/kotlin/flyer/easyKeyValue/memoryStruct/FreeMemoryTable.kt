package flyer.easyKeyValue.memoryStruct

import java.util.PriorityQueue

/**
 * 空闲表管理
 */
class FreeMemoryTable {
    private data class Node(var index: Long, var size: Long, var pre: Node? = null, var next: Node? = null)
    data class AllocateResult(val isAllocate: Boolean, val index:Long)
    private val root = Node(-1, 0)
    private val tail = Node(Long.MAX_VALUE , 0)
    private val priorityQueue = PriorityQueue<Node>(
        Comparator { a, b -> return@Comparator (b.size - a.size).toInt() }
    )


    init {
        root.next = tail
        tail.pre = root
    }

    /**
     * 添加空闲区
     * @param index 空闲区域首地址
     * @param size 空闲多少单元
     */
    fun add(index: Long, size: Long) {
        var node = root.next
        var pre: Node?
        while (node != null) {
            pre = node.pre
            if (pre != null && pre.index.plus(pre.size) <= index && index + size <= node.index) {
                insert(Node(index, size), pre, node)
                break
            }
            node = node.next
        }
    }

    /**
     * 分配空闲区
     * @param size 需要分配多少单元
     */
    fun allocate(size: Long): AllocateResult {
        if (priorityQueue.peek().size < size) {
            return AllocateResult(false, -1)
        }
        val node = priorityQueue.poll()
        remove(node)

        val result = AllocateResult(true, node.index)

        if (node.size != size) {
            node.index += size
            node.size -= size
            insert(node, node.pre!!, node.next!!)
            priorityQueue.add(node)
        }

        return result
    }


    private fun insert(node: Node, pre: Node, next: Node) {
        if (pre.index + pre.size == node.index && node.index + node.size == next.index) {
            priorityQueue.remove(next)
            priorityQueue.remove(pre)
            pre.size += node.size + next.size
            next.next?.pre = pre
            pre.next = next.next
            priorityQueue.add(pre)

        } else if (pre.index + pre.size == node.index) {
            priorityQueue.remove(pre)
            pre.size += node.size
            priorityQueue.add(pre)
        } else if (node.index + node.size == next.index) {
            priorityQueue.remove(next)
            next.index = node.index
            next.size += node.size
            priorityQueue.add(next)
        } else {
            pre.next = node
            node.pre = pre
            node.next = next
            next.pre = node
            priorityQueue.add(node)
        }

    }

    private fun remove(node: Node) {
        priorityQueue.remove(node)
        node.pre?.let { it.next = node.next }
        node.next?.let { it.pre = node.pre }
    }

    fun printTable() {
        var node = root.next
        println("==================")
        while (node != null && node != tail) {
            println("index: ${node?.index},end: ${(node?.index?.plus(node.size) ?: 0L) - 1L}, size: ${node?.size}")
            node = node.next
        }
        println("==================")
    }
}
