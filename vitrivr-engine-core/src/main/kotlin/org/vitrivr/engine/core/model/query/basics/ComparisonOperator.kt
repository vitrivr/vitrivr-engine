package org.vitrivr.engine.core.model.query.basics

import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.types.Value
import java.time.LocalDateTime
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
                is Value.DateTime -> v1.value < (v2.value as LocalDateTime)
                is Value.Double -> v1.value < (v2.value as Double)
                is Value.Float -> v1.value < (v2.value as Float)
                is Value.Int -> v1.value < (v2.value as Int)
                is Value.Long -> v1.value < (v2.value as Long)
                is Value.Short -> v1.value < (v2.value as Short)
                is Value.Text -> v1.value < (v2.value as String)
                is Value.UUIDValue -> v1.value < (v2.value as UUID)
                // This approach is for experimenting purposes, specific operators should be implemented in the future.
                is Value.GeographyValue -> {
                    val p1 = extractPointCoordinates(v1)
                    val p2 = extractPointCoordinates(v2)
                    if (p1 != null && p2 != null) {
                        val (lon1, lat1) = p1
                        val (lon2, lat2) = p2
                        lon1 < lon2 || (lon1 == lon2 && lat1 < lat2)
                    } else {
                        false
                    }
                }
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
                is Value.DateTime -> v1.value > (v2.value as LocalDateTime)
                is Value.Double -> v1.value > (v2.value as Double)
                is Value.Float -> v1.value > (v2.value as Float)
                is Value.Int -> v1.value > (v2.value as Int)
                is Value.Long -> v1.value > (v2.value as Long)
                is Value.Short -> v1.value > (v2.value as Short)
                is Value.Text -> v1.value > (v2.value as String)
                is Value.UUIDValue -> v1.value > (v2.value as UUID)
                is Value.GeographyValue -> {
                    val p1 = extractPointCoordinates(v1)
                    val p2 = extractPointCoordinates(v2)
                    if (p1 != null && p2 != null) {
                        val (lon1, lat1) = p1
                        val (lon2, lat2) = p2
                        lon1 > lon2 || (lon1 == lon2 && lat1 > lat2)
                    } else {
                        false
                    }
                }
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
                is Value.DateTime -> v1.value <= (v2.value as LocalDateTime)
                is Value.Double -> v1.value <= (v2.value as Double)
                is Value.Float -> v1.value <= (v2.value as Float)
                is Value.Int -> v1.value <= (v2.value as Int)
                is Value.Long -> v1.value <= (v2.value as Long)
                is Value.Short -> v1.value <= (v2.value as Short)
                is Value.Text -> v1.value <= (v2.value as String)
                is Value.UUIDValue -> v1.value <= (v2.value as UUID)
                is Value.GeographyValue -> {
                    val p1 = extractPointCoordinates(v1)
                    val p2 = extractPointCoordinates(v2)
                    if (p1 != null && p2 != null) {
                        val (lon1, lat1) = p1
                        val (lon2, lat2) = p2
                        lon1 < lon2 || (lon1 == lon2 && lat1 <= lat2)
                    } else {
                        // If v1 == v2 (WKT string equality), consider it true for LEQ.
                        // This handles non-point WKTs or parse failures for EQ part of LEQ.
                        if (v1.value == v2.value) true else false
                    }
                }
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
                is Value.DateTime -> v1.value >= (v2.value as LocalDateTime)
                is Value.Double -> v1.value >= (v2.value as Double)
                is Value.Float -> v1.value >= (v2.value as Float)
                is Value.Int -> v1.value >= (v2.value as Int)
                is Value.Long -> v1.value >= (v2.value as Long)
                is Value.Short -> v1.value >= (v2.value as Short)
                is Value.Text -> v1.value >= (v2.value as String)
                is Value.UUIDValue -> v1.value <= (v2.value as UUID)
                is Value.GeographyValue -> {
                    val p1 = extractPointCoordinates(v1)
                    val p2 = extractPointCoordinates(v2)
                    if (p1 != null && p2 != null) {
                        val (lon1, lat1) = p1
                        val (lon2, lat2) = p2
                        lon1 > lon2 || (lon1 == lon2 && lat1 >= lat2)
                    } else {
                        // If v1 == v2 (WKT string equality), consider it true for GEQ.
                        // This handles non-point WKTs or parse failures for EQ part of GEQ.
                        if (v1.value == v2.value) true else false
                    }
                }
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
        infix fun fromString(str: String): ComparisonOperator {
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

        /**
         * Helper function to extract longitude and latitude from a GeographyValue's WKT string.
         * Expects a simple "POINT(lon lat)" format, case-insensitive for "POINT" and tolerant of spaces.
         * Returns null if parsing fails or if it's not a GeographyValue or not a simple POINT.
         */
        private fun extractPointCoordinates(valueHolder: Value<*>): Pair<Double, Double>? {
            if (valueHolder !is Value.GeographyValue) return null
            val wkt = valueHolder.wkt
            // Regex: POINT (optional whitespace) ( (optional whitespace) LON (whitespace) LAT (optional whitespace) )
            val pattern = Regex("""POINT\s*\(\s*(-?\d+(?:\.\d+)?)\s+(-?\d+(?:\.\d+)?)\s*\)""", RegexOption.IGNORE_CASE)
            val match = pattern.matchEntire(wkt) ?: return null
            return try {
                val lon = match.groupValues[1].toDouble()
                val lat = match.groupValues[2].toDouble()
                Pair(lon, lat)
            } catch (e: NumberFormatException) {
                null
            }
        }
    }

    abstract fun <T> compare(v1: Value<T>, v2: Value<*>): Boolean

}
