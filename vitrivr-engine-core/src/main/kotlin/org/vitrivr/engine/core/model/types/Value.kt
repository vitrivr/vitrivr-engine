package org.vitrivr.engine.core.model.types

import java.util.Date

/**
 * A [Value] in vitrivr-engine maps primitive data types.
 *
 * Part of the vitrivr-engine's internal type system.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface Value<T> {

    companion object {
        fun of(value: Any): Value<*> = when (value) {
            is kotlin.String -> String(value)
            is kotlin.Boolean -> Boolean(value)
            is kotlin.Byte -> Byte(value)
            is kotlin.Short -> Short(value)
            is kotlin.Int -> Int(value)
            is kotlin.Long -> Long(value)
            is kotlin.Float -> Float(value)
            is kotlin.Double -> Double(value)
            is Date -> DateTime(value)
            else -> throw IllegalArgumentException("Unsupported data type.")
        }
    }

    val value: T

    @JvmInline
    value class String(override val value: kotlin.String) : Value<kotlin.String>

    @JvmInline
    value class Boolean(override val value: kotlin.Boolean) : Value<kotlin.Boolean>

    @JvmInline
    value class Byte(override val value: kotlin.Byte) : Value<kotlin.Byte>

    @JvmInline
    value class Short(override val value: kotlin.Short) : Value<kotlin.Short>

    @JvmInline
    value class Int(override val value: kotlin.Int) : Value<kotlin.Int>

    @JvmInline
    value class Long(override val value: kotlin.Long) : Value<kotlin.Long>

    @JvmInline
    value class Float(override val value: kotlin.Float) : Value<kotlin.Float>

    @JvmInline
    value class Double(override val value: kotlin.Double) : Value<kotlin.Double>

    @JvmInline
    value class DateTime(override val value: Date) : Value<Date>
}
