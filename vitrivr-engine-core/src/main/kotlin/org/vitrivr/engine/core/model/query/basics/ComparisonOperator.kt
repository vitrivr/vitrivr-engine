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
    EQ("=="),
    NEQ("!="),
    LE("<"),
    GR(">"),
    LEQ("<="),
    GEQ(">="),
    LIKE("~=");

    companion object{
        /**
         * Resolves a [ComparisonOperator] from the given [String].
         *
         * @param str The [String] which should be one of the [ComparisonOperator]
         * @throws IllegalArgumentException In case the given string is not one of the defined ones.
         */
        fun fromString(str: String):ComparisonOperator{
            return when(str.trim()){
                EQ.value -> EQ
                NEQ.value -> NEQ
                LE.value -> LE
                GR.value -> GR
                LEQ.value -> LEQ
                GEQ.value -> GEQ
                LIKE.value -> LIKE
                else -> throw IllegalArgumentException("Cannot parse '$str' as a comparison operator.")
            }
        }
    }
}
