package org.vitrivr.engine.core.model.query.basics

import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * Enumeration of comparison operators used by the [SimpleBooleanQuery].
 *
 * @author Ralph Gasser
 * @author Loris Sauter
 * @author Luca Rossetto
 * @version 1.2.0
 */
enum class ComparisonOperator(val value: String) {
    EQ("==") {
        override fun <T> compare(v1: Value<T>, v2: Value<*>): Boolean = v1.value == v2.value
    },
    NEQ("!=") {
        override fun <T> compare(v1: Value<T>, v2: Value<*>): Boolean = v1.value != v2.value
    },
    LE("<") {
        override fun <T> compare(v1: Value<T>, v2: Value<*>): Boolean =
            when (v1) {
                is Value.String -> v1.value < (v2.value as String)
                is Value.Boolean -> v1.value < (v2.value as Boolean)
                is Value.Byte -> v1.value < (v2.value as Byte)
                is Value.DateTime -> v1.value < (v2.value as Date)
                is Value.Double -> v1.value < (v2.value as Double)
                is Value.Float -> v1.value < (v2.value as Float)
                is Value.Int -> v1.value < (v2.value as Int)
                is Value.Long -> v1.value < (v2.value as Long)
                is Value.Short -> v1.value < (v2.value as Short)
                is Value.Text -> v1.value < (v2.value as String)
                is Value.UUIDValue -> v1.value < (v2.value as UUID)
                is Value.BooleanVector,
                is Value.DoubleVector,
                is Value.FloatVector,
                is Value.IntVector,
                is Value.LongVector -> false
            }

    },
    GR(">") {
        override fun <T> compare(v1: Value<T>, v2: Value<*>): Boolean =
            when (v1) {
                is Value.String -> v1.value > (v2.value as String)
                is Value.Boolean -> v1.value > (v2.value as Boolean)
                is Value.Byte -> v1.value > (v2.value as Byte)
                is Value.DateTime -> v1.value > (v2.value as Date)
                is Value.Double -> v1.value > (v2.value as Double)
                is Value.Float -> v1.value > (v2.value as Float)
                is Value.Int -> v1.value > (v2.value as Int)
                is Value.Long -> v1.value > (v2.value as Long)
                is Value.Short -> v1.value > (v2.value as Short)
                is Value.Text -> v1.value > (v2.value as String)
                is Value.UUIDValue -> v1.value > (v2.value as UUID)
                is Value.BooleanVector,
                is Value.DoubleVector,
                is Value.FloatVector,
                is Value.IntVector,
                is Value.LongVector -> false
            }

    },
    LEQ("<=") {
        override fun <T> compare(v1: Value<T>, v2: Value<*>): Boolean =
            when (v1) {
                is Value.String -> v1.value <= (v2.value as String)
                is Value.Boolean -> v1.value <= (v2.value as Boolean)
                is Value.Byte -> v1.value <= (v2.value as Byte)
                is Value.DateTime -> v1.value <= (v2.value as Date)
                is Value.Double -> v1.value <= (v2.value as Double)
                is Value.Float -> v1.value <= (v2.value as Float)
                is Value.Int -> v1.value <= (v2.value as Int)
                is Value.Long -> v1.value <= (v2.value as Long)
                is Value.Short -> v1.value <= (v2.value as Short)
                is Value.Text -> v1.value <= (v2.value as String)
                is Value.UUIDValue -> v1.value <= (v2.value as UUID)
                is Value.BooleanVector,
                is Value.DoubleVector,
                is Value.FloatVector,
                is Value.IntVector,
                is Value.LongVector -> false
            }

    },
    GEQ(">=") {
        override fun <T> compare(v1: Value<T>, v2: Value<*>): Boolean =
            when (v1) {
                is Value.String -> v1.value >= (v2.value as String)
                is Value.Boolean -> v1.value >= (v2.value as Boolean)
                is Value.Byte -> v1.value >= (v2.value as Byte)
                is Value.DateTime -> v1.value >= (v2.value as Date)
                is Value.Double -> v1.value >= (v2.value as Double)
                is Value.Float -> v1.value >= (v2.value as Float)
                is Value.Int -> v1.value >= (v2.value as Int)
                is Value.Long -> v1.value >= (v2.value as Long)
                is Value.Short -> v1.value >= (v2.value as Short)
                is Value.Text -> v1.value >= (v2.value as String)
                is Value.UUIDValue -> v1.value <= (v2.value as UUID)
                is Value.BooleanVector,
                is Value.DoubleVector,
                is Value.FloatVector,
                is Value.IntVector,
                is Value.LongVector -> false
            }

    },
    LIKE("~=") {
        override fun <T> compare(v1: Value<T>, v2: Value<*>): Boolean =
            when (v1) {
                is Value.String,
                is Value.Text -> {
                    (v2.value as String).replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]")
                        .replace("*", "\\*").replace("%", ".*").replace("_", ".?").toRegex().matches(v1.value as String)
                }
                else -> false
            }

    };

    companion object {
        /**
         * Resolves a [ComparisonOperator] from the given [String].
         *
         * @param str The [String] which should be one of the [ComparisonOperator]
         * @throws IllegalArgumentException In case the given string is not one of the defined ones.
         */
        fun fromString(str: String): ComparisonOperator {
            return when (str.trim()) {
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

    abstract fun <T> compare(v1: Value<T>, v2: Value<*>): Boolean

}
