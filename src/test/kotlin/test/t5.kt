package test

import test.StringUtils.STARLING.buildStringWithTemplate


object StringUtils {

    object STARLING {
        /**
         * replace single key with value in template string
         * @param template template string
         * @param key replaced string in original template
         * @param value expected string in new template
         */
        fun buildStringWithTemplate(template: String, key: String, value: String): String {
            val index = template.indexOf(key)
            if (index == -1) {
                println("[tiktokec_template_key_not_found] template = $template, key = $key")
            }
            return template.replace(key, value)
        }

        /**
         * replace keys with respective values in template string
         * @param template template string
         * @param map all <key,value> pairs
         */
        fun buildStringWithTemplate(template: String, map: HashMap<String, String>): String {
            val afterTemplate = StringBuilder(template)
            for (pair in map) {
                if (template.indexOf(pair.key) == -1) {
                    println("[tiktokec_template_key_not_found] template = $template, key = ${pair.key}")
                } else {
                    afterTemplate.replaceAll(pair.key, pair.value)
                }
            }
            return afterTemplate.toString()
        }

        /**
         * replace keys with respective values in template stringBuilder
         * @param template template stringBuilder
         * @param map all <key,value> pairs
         */
        fun buildStringWithTemplate(template: StringBuilder, map: HashMap<String, String>): StringBuilder {
            for (pair in map) {
                if (template.indexOf(pair.key) == -1) {
                    println("[tiktokec_template_key_not_found] template = $template, key = ${pair.key}")
                } else {
                    template.replaceAll(pair.key, pair.value)
                }
            }
            return template
        }


        private fun StringBuilder.replaceAll(oldValue: String, newValue: String) {
            if (oldValue.isNullOrEmpty() || newValue.isNullOrEmpty())
                return
            var index = this.indexOf(oldValue)
            if (index > -1 && oldValue != newValue) {
                var lastIndex = 0
                while (index > -1) {
                    this.replace(index, index + oldValue.length, newValue)
                    lastIndex = index + newValue.length
                    index = this.indexOf(oldValue, lastIndex)
                }
            }
        }


    }

}



fun main() {
    val str = "this is a example \${key} string \${key} \${key2}"
    val builder = StringBuilder(str)
    println(buildStringWithTemplate(builder, hashMapOf(
        "\${key}" to "value",
        "1" to "2",
        "\${key2}" to "value2"
    )

    ).toString())
}