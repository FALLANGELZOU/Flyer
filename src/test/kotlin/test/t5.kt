package test

class t5 {
}

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
fun buildStringWithTemplate(template: String, map: HashMap<String, String>, mode: String = "native"): String {
    var afterTemplate = template
    for (pair in map) {
        if (template.indexOf(pair.key) == -1) {
            println("[tiktokec_template_key_not_found] template = $template, key = ${pair.key}")
        } else {
            afterTemplate = afterTemplate.replace(pair.key, pair.value)
        }
    }
    return afterTemplate
}

fun main() {
    val str = "this is a example \${key} string \${key}"
    println(buildStringWithTemplate(str, hashMapOf(
        Pair("1","1"),
        Pair("\${key}", "value")
    ))
    )
}