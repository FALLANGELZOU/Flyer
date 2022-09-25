package flyer.easyKeyValue

import java.text.SimpleDateFormat

object KVUtil {
    object Bytes {

        private fun printBytes(bytes: ByteArray) {
            if (bytes.isEmpty()) {
                println("++\n" +
                        "||\n" +
                        "++")
                return
            }
            val strPre = StringBuffer()
            strPre.append("+")
            val strBytes = StringBuffer()
            strBytes.append("|")

            for(byte in bytes) {
                strPre.append("--------+")

                for (i in 7 downTo 0) {
                    strBytes.append((byte.toInt() shr i and 1))
                }

                strBytes.append("|")
            }
            println(strPre.toString())
            println(strBytes.toString())
            println(strPre.toString())
        }

        fun int2Byte4(num: Int): ByteArray {
            var byteArray = ByteArray(4)
            var highH = ((num shr 24) and 0xff).toByte()
            var highL = ((num shr 16) and 0xff).toByte()
            var LowH = ((num shr 8) and 0xff).toByte()
            var LowL = (num and 0xff).toByte()
            byteArray[0] = highH
            byteArray[1] = highL
            byteArray[2] = LowH
            byteArray[3] = LowL
            return byteArray
        }

        fun int2Byte2(num: Int): ByteArray {
            var byteArray = ByteArray(2)
            var LowH = ((num shr 8) and 0xff).toByte()
            var LowL = (num and 0xff).toByte()
            byteArray[0] = LowH
            byteArray[1] = LowL
            return byteArray
        }

        fun ByteArray.toHexString(hasSpace: Boolean = true) = this.joinToString("") {
            (it.toInt() and 0xFF).toString(16).padStart(2, '0').toUpperCase() + if (hasSpace) " " else ""
        }

        fun ByteArray.toAsciiString(hasSpace: Boolean = false) =
            this.map { it.toChar() }.joinToString(if (hasSpace) " " else "")

        fun ByteArray.readInt8(offset: Int = 0): Int {
            throwOffestError(this, offset, 1)
            return this[offset].toInt()
        }

        fun ByteArray.readUInt8(offset: Int = 0): Int {
            throwOffestError(this, offset, 1)
            return this[offset].toInt() and 0xFF
        }

        fun ByteArray.readInt16BE(offset: Int = 0): Int {
            throwOffestError(this, offset, 2)
            return (this[offset].toInt() shl 8) + (this[offset + 1].toInt() and 0xFF)
        }

        fun ByteArray.readUInt16BE(offset: Int = 0): Int {
            throwOffestError(this, offset, 2)
            // 方式一
            return ((this[offset].toInt() and 0xFF) shl 8) or (this[offset + 1].toInt() and 0xFF)
            // 方式二
//    return readUnsigned(this, 2, offset, false).toInt()
        }


        fun ByteArray.readInt16LE(offset: Int = 0): Int {
            throwOffestError(this, offset, 2)
            return (this[offset + 1].toInt() shl 8) + (this[offset].toInt() and 0xFF)
        }

        fun ByteArray.readUInt16LE(offset: Int = 0): Int {
            throwOffestError(this, offset, 2)
            return ((this[offset + 1].toInt() and 0xFF) shl 8) or (this[offset].toInt() and 0xFF)
//    return readUnsigned(this, 2, offset, true).toInt()
        }

        fun ByteArray.readInt32BE(offset: Int = 0): Int {
            throwOffestError(this, offset, 4)
            return (this[offset].toInt()) shl 24 or
                    ((this[offset + 1].toInt() and 0xFF) shl 16) or
                    ((this[offset + 2].toInt() and 0xFF) shl 8) or
                    (this[offset + 3].toInt() and 0xFF)
//    return (this[offset].toInt() shl 24) + (this[offset + 1].toUByte().toInt() shl 16) + (this[offset + 2].toUByte().toInt() shl 8) + this[offset + 3].toUByte().toInt()
        }

        fun ByteArray.readUInt32BE(offset: Int = 0): Long {
            throwOffestError(this, offset, 4)
            return (((this[offset].toInt() and 0xFF).toLong() shl 24) or
                    ((this[offset + 1].toInt() and 0xFF).toLong() shl 16) or
                    ((this[offset + 2].toInt() and 0xFF).toLong() shl 8) or
                    (this[offset + 3].toInt() and 0xFF).toLong())
//    return readUnsigned(this, 4, offset, false)
        }

        fun ByteArray.readInt32LE(offset: Int = 0): Int {
            throwOffestError(this, offset, 4)
            return (this[offset + 3].toInt()) shl 24 or
                    ((this[offset + 2].toInt() and 0xFF) shl 16) or
                    ((this[offset + 1].toInt() and 0xFF) shl 8) or
                    (this[offset].toInt() and 0xFF)
//    return (this[offset + 3].toInt() shl 24) + (this[offset + 2].toUByte().toInt() shl 16) + (this[offset + 1].toUByte().toInt() shl 8) + this[offset].toUByte().toInt()
        }

        fun ByteArray.readUInt32LE(offset: Int = 0): Long {
            throwOffestError(this, offset, 4)
            return (((this[offset + 3].toInt() and 0xFF).toLong() shl 24) or
                    ((this[offset + 2].toInt() and 0xFF).toLong() shl 16) or
                    ((this[offset + 1].toInt() and 0xFF).toLong() shl 8) or
                    (this[offset].toInt() and 0xFF).toLong())
        }

        // 四字节 float
        fun ByteArray.readFloatBE(offset: Int = 0) = java.lang.Float.intBitsToFloat(this.readInt32BE(offset))
        // 四字节 float
        fun ByteArray.readFloatLE(offset: Int = 0) = java.lang.Float.intBitsToFloat(this.readInt32LE(offset))

        // 四字节 时间
        fun ByteArray.readTimeBE(offset: Int = 0, pattern: String = "yyyy-MM-dd HH:mm:ss") =
            SimpleDateFormat(pattern).format(this.readUInt32BE(offset) * 1000)

        fun ByteArray.readTimeLE(offset: Int = 0, pattern: String = "yyyy-MM-dd HH:mm:ss") =
            SimpleDateFormat(pattern).format(this.readUInt32LE(offset) * 1000)

        // 读取ByteArray
        fun ByteArray.readByteArrayBE(offset: Int, byteLength: Int): ByteArray {
            throwOffestError(this, offset)
            return this.copyOfRange(offset, if ((offset + byteLength) > this.size) this.size else offset + byteLength)
        }

        fun ByteArray.readByteArrayLE(offset: Int, byteLength: Int): ByteArray {
            throwOffestError(this, offset)
            return this.readByteArrayBE(offset, byteLength).reversedArray()
        }

        // 读取指定范围输出字符串
        fun ByteArray.readStringBE(
            offset: Int,
            byteLength: Int,
            encoding: String = "hex",
            hasSpace: Boolean = encoding.toLowerCase() == "hex"
        ): String {
            return when (encoding.toLowerCase()) {
                "hex" -> this.readByteArrayBE(offset, byteLength).toHexString(hasSpace)
                "ascii" -> this.readByteArrayBE(offset, byteLength).map { it.toChar() }.joinToString(if (hasSpace) " " else "")
                else -> ""
            }
        }

        fun ByteArray.readStringLE(
            offset: Int,
            byteLength: Int,
            encoding: String = "hex",
            hasSpace: Boolean = encoding.toLowerCase() == "hex"
        ): String {
            return when (encoding.toLowerCase()) {
                "hex" -> this.readByteArrayLE(offset, byteLength).toHexString(hasSpace)
                "ascii" -> this.readByteArrayLE(offset, byteLength).map { it.toChar() }.joinToString(if (hasSpace) " " else "")
                else -> ""
            }
        }

        // ********************************************** 写 **********************************************
        fun ByteArray.writeInt8(value: Int, offset: Int = 0): ByteArray {
            throwOffestError(this, offset)
            // 无符号Int 执行写入
            this[offset] = value.toByte()
            return this
        }

        fun ByteArray.writeInt16BE(value: Int, offset: Int = 0): ByteArray {
            throwOffestError(this, offset, 2)
            // 执行写入
            this[offset] = (value and 0xff00 ushr 8).toByte()
            this[offset + 1] = (value and 0xff).toByte()
            return this
        }

        fun ByteArray.writeInt16LE(value: Int, offset: Int = 0): ByteArray {
            throwOffestError(this, offset, 2)
            // 无符号Int 执行写入
            this[offset] = (value and 0xff).toByte()
            this[offset + 1] = (value and 0xff00 ushr 8).toByte()
            return this
        }

        fun ByteArray.writeInt32BE(value: Long, offset: Int = 0): ByteArray {
            throwOffestError(this, offset, 4)
            // 无符号Int 执行写入
            this[offset + 3] = (value and 0xff).toByte()
            this[offset + 2] = (value and 0xff00 ushr 8).toByte()
            this[offset + 1] = (value and 0xff0000 ushr 16).toByte()
            this[offset] = (value and 0xff000000 ushr 24).toByte()
            return this
        }

        fun ByteArray.writeInt32LE(value: Long, offset: Int = 0): ByteArray {
            throwOffestError(this, offset, 4)
            // 无符号Int 执行写入
            this[offset] = (value and 0xff).toByte()
            this[offset + 1] = (value and 0xff00 ushr 8).toByte()
            this[offset + 2] = (value and 0xff0000 ushr 16).toByte()
            this[offset + 3] = (value and 0xff000000 ushr 24).toByte()
            return this
        }

        // 写入Float类型
        fun ByteArray.writeFloatBE(value: Float, offset: Int = 0): ByteArray {
            throwOffestError(this, offset, 4)
            this.writeInt32BE(java.lang.Float.floatToIntBits(value).toLong(), offset)
            return this
        }

        fun ByteArray.writeFloatLE(value: Float, offset: Int = 0): ByteArray {
            throwOffestError(this, offset, 4)
            this.writeInt32LE(java.lang.Float.floatToIntBits(value).toLong(), offset)
            return this
        }

        // 写入时间
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun ByteArray.writeTimeBE(time: String, offset: Int = 0, pattern: String = "yyyy-MM-dd HH:mm:ss"): ByteArray {
            this.writeInt32BE(SimpleDateFormat(pattern).parse(time).time / 1000, offset)
            return this
        }

        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun ByteArray.writeTimeLE(time: String, offset: Int = 0, pattern: String = "yyyy-MM-dd HH:mm:ss"): ByteArray {
            this.writeInt32LE(SimpleDateFormat(pattern).parse(time).time / 1000, offset)
            return this
        }

        // 指定位置写入ByteArray
        fun ByteArray.writeByteArrayBE(byteArray: ByteArray, offset: Int = 0, length: Int = byteArray.size): ByteArray {
            this.writeStringBE(byteArray.toHexString(), offset, length)
            return this
        }

        fun ByteArray.writeByteArrayLE(byteArray: ByteArray, offset: Int = 0, length: Int = byteArray.size): ByteArray {
            this.writeStringLE(byteArray.toHexString(), offset, length)
            return this
        }

        // 指定位置插入ByteArray
        fun ByteArray.insertByteArrayBE(
            insertArray: ByteArray,
            origrinalIndex: Int = 0,
            insertArrayOffset: Int = 0,
            insertArrayLength: Int = insertArray.size - insertArrayOffset
        ): ByteArray {
            val byteArrayPre = this.copyOfRange(0, origrinalIndex)
            val byteArrayLast = this.copyOfRange(origrinalIndex, this.size)
            val insertFinalArray = insertArray.copyOfRange(insertArrayOffset, insertArrayOffset + insertArrayLength)
            return byteArrayPre.plus(insertFinalArray).plus(byteArrayLast)
        }
        fun ByteArray.insertByteArrayLE(
            insertArray: ByteArray,
            origrinalIndex: Int = 0,
            insertArrayOffset: Int = 0,
            insertArrayLength: Int = insertArray.size - insertArrayOffset
        ): ByteArray {
            insertArray.reverse()
            val byteArrayPre = this.copyOfRange(0, origrinalIndex)
            val byteArrayLast = this.copyOfRange(origrinalIndex, this.size)
            val insertFinalArray = insertArray.copyOfRange(insertArrayOffset, insertArrayOffset + insertArrayLength)
            return byteArrayPre.plus(insertFinalArray).plus(byteArrayLast)
        }

// 指定位置写入String
        /**
         * @str 写入的字符串
         * @encoding  Hex & ASCII
         */
        fun ByteArray.writeStringBE(str: String, offset: Int = 0, encoding: String = "hex"): ByteArray {
            throwOffestError(this, offset)
            when (encoding.toLowerCase()) {
                "hex" -> {
                    val hex = str.replace(" ", "")
                    throwHexError(hex)
                    for (i in 0 until hex.length / 2) {
                        if (i + offset < this.size) {
                            this[i + offset] = hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                        }
                    }
                }
                "ascii" -> {
                    val hex = str.toCharArray().map { it.toInt() }.map { it.toString(16) }.joinToString("")
                    this.writeStringBE(hex, offset, "hex")
                }
            }
            return this
        }

        // length: 写入长度
        fun ByteArray.writeStringLE(str: String, offset: Int = 0, encoding: String = "hex"): ByteArray {
            when (encoding.toLowerCase()) {
                "hex" -> {
                    val hex = str.reversalEvery2Charts()
                    this.writeStringBE(hex, offset, encoding)
                }
                "ascii" -> {
                    val hex = str.toCharArray().map { it.toInt() }.map { it.toString(16) }.joinToString("")
                    this.writeStringLE(hex, offset, "hex")
                }
            }
            return this
        }

        fun ByteArray.writeStringBE(str: String, offset: Int, length: Int, encoding: String = "hex"): ByteArray {
            throwOffestError(this, offset, length)
            when (encoding.toLowerCase()) {
                "hex" -> {
                    val hex = str.replace(" ", "").padStart(length * 2, '0').substring(0, length * 2)
                    throwHexError(hex)
                    this.writeStringBE(hex, offset)
                }
                "ascii" -> {
                    val hex = str.toCharArray().map { it.toInt() }.map { it.toString(16) }.joinToString("")
                    this.writeStringBE(hex, offset, length, "hex")
                }
            }
            return this
        }

        fun ByteArray.writeStringLE(str: String, offset: Int, length: Int, encoding: String = "hex"): ByteArray {
            when (encoding.toLowerCase()) {
                "hex" -> {
                    val hex = str.reversalEvery2Charts().padEnd(length * 2, '0').substring(0, length * 2)
                    this.writeStringBE(hex, offset, length, encoding)
                }
                "ascii" -> {
                    val hex = str.toCharArray().map { it.toInt() }.map { it.toString(16) }.joinToString("")
                    this.writeStringLE(hex, offset, length, "hex")
                }
            }
            return this
        }

        // 无符号int
        private fun readUnsigned(byteArray: ByteArray, len: Int, offset: Int, littleEndian: Boolean): Long {
            var value = 0L
            for (count in 0 until len) {
                val shift = (if (littleEndian) count else (len - 1 - count)) shl 3
                value = value or (0xff.toLong() shl shift and (byteArray[offset + count].toLong() shl shift))
            }
            return value
        }

        /****  异常  ****/
        private fun throwLenError(byteArray: ByteArray, byteLength: Int) {
            if (byteLength <= 0 || byteLength > 4) throw IllegalArgumentException("The value of \"byteLength\" is out of range. It must be >= 1 and <= 4. Received ${byteLength}")
            if (byteLength > byteArray.size) throw IllegalArgumentException("Attempt to write outside ByteArray bounds.")
        }

        private fun throwHexError(hex: String) {
            if (hex.length % 2 != 0) throw IllegalArgumentException("The value of \"hex\".length is out of range. It must be an even number")
        }

        private fun throwOffestError(byteArray: ByteArray, offset: Int, length: Int = 1, byteLength: Int = 0) {
            if (offset > byteArray.size - length - byteLength) throw IllegalArgumentException("The value of \"offset\" is out of range. It must be >= 0 and <= ${byteArray.size - length - byteLength}. Received ${offset}")
        }

        fun String.reversalEvery2Charts(hasSpace: Boolean = false): String {
            val hex = this.addSpaceEvery2Charts()
            return hex.split(" ").reversed().joinToString(if (hasSpace) " " else "")
        }

        fun String.addSpaceEvery2Charts(): String {
            val hex = this.replace(" ", "")
            val sb = StringBuilder()
            for (i in 0 until hex.length / 2) {
                sb.append(hex.substring(i * 2, i * 2 + 2))
                sb.append(" ")
            }
            return sb.toString().trim()
        }

        fun String.hex2ByteArray(): ByteArray {
            val s = this.replace(" ", "")
            val bs = ByteArray(s.length/2)
            for (i in 0 until s.length/2){
                bs[i] = s.substring(i*2, i*2+2).toInt(16).toByte()
            }
            return bs
        }

        fun String.ascii2ByteArray(hasSpace: Boolean = false): ByteArray {
            val s = if(hasSpace) this else this.replace(" ", "")
            return s.toByteArray(charset("US-ASCII"))
        }

        fun String.addFirst(s: String) = "$s$this"

        fun String.addLast(s: String) = "$this$s"

    }

    object ByteList {
        fun ArrayList<Byte>.toHexString(hasSpace: Boolean = true) = this.joinToString("") {
            (it.toInt() and 0xFF).toString(16).padStart(2, '0').toUpperCase() + if (hasSpace) " " else ""
        }

        fun ArrayList<Byte>.toAsciiString(hasSpace: Boolean = false) =
            this.map { it.toChar() }.joinToString(if (hasSpace) " " else "")

        fun ArrayList<Byte>.readInt8(offset: Int = 0): Int {
            throwOffestError(this, offset, 1)
            return this[offset].toInt()
        }

        fun ArrayList<Byte>.readUInt8(offset: Int = 0): Int {
            throwOffestError(this, offset, 1)
            return this[offset].toInt() and 0xFF
        }

        fun ArrayList<Byte>.readInt16BE(offset: Int = 0): Int {
            throwOffestError(this, offset, 2)
            return (this[offset].toInt() shl 8) + (this[offset + 1].toInt() and 0xFF)
        }

        fun ArrayList<Byte>.readUInt16BE(offset: Int = 0): Int {
            throwOffestError(this, offset, 2)
            // 方式一
            return ((this[offset].toInt() and 0xFF) shl 8) or (this[offset + 1].toInt() and 0xFF)
            // 方式二
//    return readUnsigned(this, 2, offset, false).toInt()
        }


        fun ArrayList<Byte>.readInt16LE(offset: Int = 0): Int {
            throwOffestError(this, offset, 2)
            return (this[offset + 1].toInt() shl 8) + (this[offset].toInt() and 0xFF)
        }

        fun ArrayList<Byte>.readUInt16LE(offset: Int = 0): Int {
            throwOffestError(this, offset, 2)
            return ((this[offset + 1].toInt() and 0xFF) shl 8) or (this[offset].toInt() and 0xFF)
//    return readUnsigned(this, 2, offset, true).toInt()
        }

        fun ArrayList<Byte>.readInt32BE(offset: Int = 0): Int {
            throwOffestError(this, offset, 4)
            return (this[offset].toInt()) shl 24 or
                    ((this[offset + 1].toInt() and 0xFF) shl 16) or
                    ((this[offset + 2].toInt() and 0xFF) shl 8) or
                    (this[offset + 3].toInt() and 0xFF)
//    return (this[offset].toInt() shl 24) + (this[offset + 1].toUByte().toInt() shl 16) + (this[offset + 2].toUByte().toInt() shl 8) + this[offset + 3].toUByte().toInt()
        }

        fun ArrayList<Byte>.readUInt32BE(offset: Int = 0): Long {
            throwOffestError(this, offset, 4)
            return (((this[offset].toInt() and 0xFF).toLong() shl 24) or
                    ((this[offset + 1].toInt() and 0xFF).toLong() shl 16) or
                    ((this[offset + 2].toInt() and 0xFF).toLong() shl 8) or
                    (this[offset + 3].toInt() and 0xFF).toLong())
//    return readUnsigned(this, 4, offset, false)
        }

        fun ArrayList<Byte>.readInt32LE(offset: Int = 0): Int {
            throwOffestError(this, offset, 4)
            return (this[offset + 3].toInt()) shl 24 or
                    ((this[offset + 2].toInt() and 0xFF) shl 16) or
                    ((this[offset + 1].toInt() and 0xFF) shl 8) or
                    (this[offset].toInt() and 0xFF)
//    return (this[offset + 3].toInt() shl 24) + (this[offset + 2].toUByte().toInt() shl 16) + (this[offset + 1].toUByte().toInt() shl 8) + this[offset].toUByte().toInt()
        }

        fun ArrayList<Byte>.readUInt32LE(offset: Int = 0): Long {
            throwOffestError(this, offset, 4)
            return (((this[offset + 3].toInt() and 0xFF).toLong() shl 24) or
                    ((this[offset + 2].toInt() and 0xFF).toLong() shl 16) or
                    ((this[offset + 1].toInt() and 0xFF).toLong() shl 8) or
                    (this[offset].toInt() and 0xFF).toLong())
//    return readUnsigned(this, 4, offset, true)
        }

        // 四字节 float
        fun ArrayList<Byte>.readFloatBE(offset: Int = 0) = java.lang.Float.intBitsToFloat(this.readInt32BE(offset))
        // 四字节 float
        fun ArrayList<Byte>.readFloatLE(offset: Int = 0) = java.lang.Float.intBitsToFloat(this.readInt32LE(offset))
        // 四字节 时间
        fun ArrayList<Byte>.readTimeBE(offset: Int = 0, pattern: String = "yyyy-MM-dd HH:mm:ss") =
            SimpleDateFormat(pattern).format(this.readUInt32BE(offset) * 1000)

        fun ArrayList<Byte>.readTimeLE(offset: Int = 0, pattern: String = "yyyy-MM-dd HH:mm:ss") =
            SimpleDateFormat(pattern).format(this.readUInt32LE(offset) * 1000)

        // 读取ByteArray
        fun ArrayList<Byte>.readByteArrayBE(offset: Int, byteLength: Int): ArrayList<Byte> {
            throwOffestError(this, offset)
            return this.subList(offset, if ((offset + byteLength) > this.size) this.size else offset + byteLength) as ArrayList<Byte>
        }

        fun ArrayList<Byte>.readByteArrayLE(offset: Int, byteLength: Int): ArrayList<Byte> {
            throwOffestError(this, offset)
            return this.readByteArrayBE(offset, byteLength).reversed() as ArrayList<Byte>
        }

        // 读取指定范围输出字符串
        fun ArrayList<Byte>.readStringBE(
            offset: Int,
            byteLength: Int,
            encoding: String = "hex",
            hasSpace: Boolean = encoding.toLowerCase() == "hex"
        ): String {
            return when (encoding.toLowerCase()) {
                "hex" -> this.readByteArrayBE(offset, byteLength).toHexString(hasSpace)
                "ascii" -> this.readByteArrayBE(offset, byteLength).map { it.toChar() }.joinToString(if (hasSpace) " " else "")
                else -> ""
            }
        }

        fun ArrayList<Byte>.readStringLE(
            offset: Int,
            byteLength: Int,
            encoding: String = "hex",
            hasSpace: Boolean = encoding.toLowerCase() == "hex"
        ): String {
            return when (encoding.toLowerCase()) {
                "hex" -> this.readByteArrayLE(offset, byteLength).toHexString(hasSpace)
                "ascii" -> this.readByteArrayLE(offset, byteLength).map { it.toChar() }.joinToString(if (hasSpace) " " else "")
                else -> ""
            }
        }

        // ********************************************** 写 **********************************************
        fun ArrayList<Byte>.writeInt8(value: Int, offset: Int = 0): ArrayList<Byte> {
            throwOffestError(this, offset)
            // 无符号Int 执行写入
            this[offset] = value.toByte()
            return this
        }

        fun ArrayList<Byte>.writeInt16BE(value: Int, offset: Int = 0): ArrayList<Byte> {
            throwOffestError(this, offset, 2)
            // 执行写入
            this[offset] = (value and 0xff00 ushr 8).toByte()
            this[offset + 1] = (value and 0xff).toByte()
            return this
        }

        fun ArrayList<Byte>.writeInt16LE(value: Int, offset: Int = 0): ArrayList<Byte> {
            throwOffestError(this, offset, 2)
            // 无符号Int 执行写入
            this[offset] = (value and 0xff).toByte()
            this[offset + 1] = (value and 0xff00 ushr 8).toByte()
            return this
        }

        fun ArrayList<Byte>.writeInt32BE(value: Long, offset: Int = 0): ArrayList<Byte> {
            throwOffestError(this, offset, 4)
            // 无符号Int 执行写入
            this[offset + 3] = (value and 0xff).toByte()
            this[offset + 2] = (value and 0xff00 ushr 8).toByte()
            this[offset + 1] = (value and 0xff0000 ushr 16).toByte()
            this[offset] = (value and 0xff000000 ushr 24).toByte()
            return this
        }

        fun ArrayList<Byte>.writeInt32LE(value: Long, offset: Int = 0): ArrayList<Byte> {
            throwOffestError(this, offset, 4)
            // 无符号Int 执行写入
            this[offset] = (value and 0xff).toByte()
            this[offset + 1] = (value and 0xff00 ushr 8).toByte()
            this[offset + 2] = (value and 0xff0000 ushr 16).toByte()
            this[offset + 3] = (value and 0xff000000 ushr 24).toByte()
            return this
        }

        // 写入Float类型
        fun ArrayList<Byte>.writeFloatBE(value: Float, offset: Int = 0): ArrayList<Byte> {
            throwOffestError(this, offset, 4)
            this.writeInt32BE(java.lang.Float.floatToIntBits(value).toLong(), offset)
            return this
        }

        fun ArrayList<Byte>.writeFloatLE(value: Float, offset: Int = 0): ArrayList<Byte> {
            throwOffestError(this, offset, 4)
            this.writeInt32LE(java.lang.Float.floatToIntBits(value).toLong(), offset)
            return this
        }

        // 写入时间
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun ArrayList<Byte>.writeTimeBE(time: String, offset: Int = 0, pattern: String = "yyyy-MM-dd HH:mm:ss"): ArrayList<Byte> {
            this.writeInt32BE(SimpleDateFormat(pattern).parse(time).time / 1000, offset)
            return this
        }

        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun ArrayList<Byte>.writeTimeLE(time: String, offset: Int = 0, pattern: String = "yyyy-MM-dd HH:mm:ss"): ArrayList<Byte> {
            this.writeInt32LE(SimpleDateFormat(pattern).parse(time).time / 1000, offset)
            return this
        }

        // 指定位置写入ByteArray
        fun ArrayList<Byte>.writeByteArrayBE(list: ArrayList<Byte>, offset: Int = 0, length: Int = list.size): ArrayList<Byte> {
            this.writeStringBE(list.toHexString(), offset, length)
            return this
        }

        fun ArrayList<Byte>.writeByteArrayLE(list: ArrayList<Byte>, offset: Int = 0, length: Int = list.size): ArrayList<Byte> {
            this.writeStringLE(list.toHexString(), offset, length)
            return this
        }


// 指定位置写入String
        /**
         * @str 写入的字符串
         * @encoding  Hex & ASCII
         */
        fun ArrayList<Byte>.writeStringBE(str: String, offset: Int = 0, encoding: String = "hex"): ArrayList<Byte> {
            throwOffestError(this, offset)
            when (encoding.toLowerCase()) {
                "hex" -> {
                    val hex = str.replace(" ", "")
                    throwHexError(hex)
                    for (i in 0 until hex.length / 2) {
                        if (i + offset < this.size) {
                            this[i + offset] = hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                        }
                    }
                }
                "ascii" -> {
                    val hex = str.toCharArray().map { it.toInt() }.map { it.toString(16) }.joinToString("")
                    this.writeStringBE(hex, offset, "hex")
                }
            }
            return this
        }

        // length: 写入长度
        fun ArrayList<Byte>.writeStringLE(str: String, offset: Int = 0, encoding: String = "hex"): ArrayList<Byte> {
            when (encoding.toLowerCase()) {
                "hex" -> {
                    val hex = str.reversalEvery2Charts()
                    this.writeStringBE(hex, offset, encoding)
                }
                "ascii" -> {
                    val hex = str.toCharArray().map { it.toInt() }.map { it.toString(16) }.joinToString("")
                    this.writeStringLE(hex, offset, "hex")
                }
            }
            return this
        }

        fun ArrayList<Byte>.writeStringBE(str: String, offset: Int, length: Int, encoding: String = "hex"): ArrayList<Byte> {
            throwOffestError(this, offset, length)
            when (encoding.toLowerCase()) {
                "hex" -> {
                    val hex = str.replace(" ", "").padStart(length * 2, '0').substring(0, length * 2)
                    throwHexError(hex)
                    this.writeStringBE(hex, offset)
                }
                "ascii" -> {
                    val hex = str.toCharArray().map { it.toInt() }.map { it.toString(16) }.joinToString("")
                    this.writeStringBE(hex, offset, length, "hex")
                }
            }
            return this
        }

        fun ArrayList<Byte>.writeStringLE(str: String, offset: Int, length: Int, encoding: String = "hex"): ArrayList<Byte> {
            when (encoding.toLowerCase()) {
                "hex" -> {
                    val hex = str.reversalEvery2Charts().padEnd(length * 2, '0').substring(0, length * 2)
                    this.writeStringBE(hex, offset, length, encoding)
                }
                "ascii" -> {
                    val hex = str.toCharArray().map { it.toInt() }.map { it.toString(16) }.joinToString("")
                    this.writeStringLE(hex, offset, length, "hex")
                }
            }
            return this
        }

        // 无符号int
        private fun readUnsigned(list: ArrayList<Byte>, len: Int, offset: Int, littleEndian: Boolean): Long {
            var value = 0L
            for (count in 0 until len) {
                val shift = (if (littleEndian) count else (len - 1 - count)) shl 3
                value = value or (0xff.toLong() shl shift and (list[offset + count].toLong() shl shift))
            }
            return value
        }

        /****  异常  ****/
        private fun throwLenError(list: ArrayList<Byte>, byteLength: Int) {
            if (byteLength <= 0 || byteLength > 4) throw IllegalArgumentException("The value of \"byteLength\" is out of range. It must be >= 1 and <= 4. Received ${byteLength}")
            if (byteLength > list.size) throw IllegalArgumentException("Attempt to write outside ArrayList<Byte> bounds.")
        }

        private fun throwHexError(hex: String) {
            if (hex.length % 2 != 0) throw IllegalArgumentException("The value of \"hex\".length is out of range. It must be an even number")
        }

        private fun throwOffestError(list: ArrayList<Byte>, offset: Int, length: Int = 1, byteLength: Int = 0) {
            if (offset > list.size - length - byteLength) throw IllegalArgumentException("The value of \"offset\" is out of range. It must be >= 0 and <= ${list.size - length - byteLength}. Received ${offset}")
        }

        fun String.reversalEvery2Charts(hasSpace: Boolean = false): String {
            val hex = this.addSpaceEvery2Charts()
            return hex.split(" ").reversed().joinToString(if (hasSpace) " " else "")
        }

        fun String.addSpaceEvery2Charts(): String {
            val hex = this.replace(" ", "")
            val sb = StringBuilder()
            for (i in 0 until hex.length / 2) {
                sb.append(hex.substring(i * 2, i * 2 + 2))
                sb.append(" ")
            }
            return sb.toString().trim()
        }

        fun String.hex2ByteArray(): ByteArray {
            val s = this.replace(" ", "")
            val bs = ByteArray(s.length/2)
            for (i in 0 until s.length/2){
                bs[i] = s.substring(i*2, i*2+2).toInt(16).toByte()
            }
            return bs
        }

        fun String.ascii2ByteArray(hasSpace: Boolean = false): ByteArray {
            val s = if(hasSpace) this else this.replace(" ", "")
            return s.toByteArray(charset("US-ASCII"))
        }

        fun String.addFirst(s: String) = "$s$this"

        fun String.addLast(s: String) = "$this$s"
    }

}