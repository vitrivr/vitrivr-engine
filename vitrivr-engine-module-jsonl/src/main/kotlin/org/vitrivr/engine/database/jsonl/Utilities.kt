package org.vitrivr.engine.database.jsonl

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */


/**
 * Converts a LIKE pattern to an equivalent regular expression.
 *
 * @return Regular expression that is equivalent to the LIKE pattern.
 */
fun String.likeToRegex(): Regex {
    val regex = StringBuilder(this.length * 2)
    regex.append('^') // Start of the line

    var i = 0
    while (i < this.length) {
        when (val c = this[i]) {
            '%' -> regex.append(".*")
            '_' -> regex.append('.')
            '\\' -> {
                if (i + 1 < this.length) {
                    val next = this[i + 1]
                    if (next == '%' || next == '_' || next == '\\') {
                        regex.append('\\').append(next)
                        i++
                    } else {
                        regex.append("\\\\")
                    }
                } else {
                    regex.append("\\\\")
                }
            }

            else -> {
                if ("[](){}.*+?$^|#".indexOf(c) >= 0) {
                    regex.append('\\')
                }
                regex.append(c)
            }
        }
        i++
    }
    regex.append('$') // End of the line
    return regex.toString().toRegex()
}