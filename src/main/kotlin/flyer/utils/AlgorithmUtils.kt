package flyer.utils


object AlgorithmUtils {
    class Trie() {
        data class Node(val char: Char) {
            val children = mutableMapOf <Char, Node>()
            var cnt: Int = 0
        }
        private val root  = Node(' ')

        fun insert(word: String) {
            var node = root
            for (char in word) {
                if (node.children[char] == null) {
                    node.children[char] = Node(char)
                }
                node = node.children[char]!!
            }
            node.cnt ++
        }

        fun match(word: String): Boolean {
            var node = root
            for (char in word) {
                if (node.children[char] == null) return false
                node = node.children[char]!!
            }
            if (node.cnt > 0) return true
            return false
        }
    }
    class AhoCorasickAutomaton() {
        private val tr = Trie()
    }
}

fun main() {
    val tr = AlgorithmUtils.Trie()
    tr.insert("apple")
    tr.insert("balance")
    tr.insert("key")
    println(tr.match("key"))
    println(tr.match("key2"))

}