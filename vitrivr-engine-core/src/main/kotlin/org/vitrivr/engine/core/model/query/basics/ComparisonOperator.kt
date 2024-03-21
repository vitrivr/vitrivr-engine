package org.vitrivr.engine.core.model.query.basics

import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery

/**
 * Enumeration of comparison operators used by the [SimpleBooleanQuery].
 *
 * @author Ralph Gasser
 * @author Loris Sauter
 * @version 1.1.0
 */
enum class ComparisonOperator(val value: String) {
    EQ("="),
    NEQ("!="),
    LE("<"),
    GR(">"),
    LEQ("<="),
    GEQ(">=");

    fun fromString(str: String):ComparisonOperator{
        return when(str){
            "=" -> EQ
            "!=" -> NEQ
            "<" -> LE
            ">" -> GR
            "<=" -> LEQ
            ">=" -> GEQ
            else -> throw IllegalArgumentException("Cannot parse '$str' as a comparison operator.")
        }
    }
}
