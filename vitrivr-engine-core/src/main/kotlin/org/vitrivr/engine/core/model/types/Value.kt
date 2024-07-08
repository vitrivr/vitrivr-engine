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

    /** The actual, primitive value. */
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

    /**
     * A [Vector] in vitrivr-engine maps primitive data types.
     *
     * Part of the vitrivr-engine's internal type system.
     *
     * @param T The type of the elements in the [Vector].
     * @property size The size of the [Vector].
     * @property value The actual, primitive value.
     * @constructor Creates a new [Vector] with the given [value].
     */
    sealed interface Vector<T> : Value<T> {
        val size: kotlin.Int
    }


    @JvmInline
    value class BooleanVector(override val value: BooleanArray) : Vector<BooleanArray> {
        constructor(size: kotlin.Int, init: (kotlin.Int) -> kotlin.Boolean = { false }) : this(BooleanArray(size, init))

        override val size: kotlin.Int
            get() = this.value.size
    }

    @JvmInline
    value class IntVector(override val value: IntArray) : Vector<IntArray> {
        constructor(size: kotlin.Int, init: (kotlin.Int) -> kotlin.Int = { 0 }) : this(IntArray(size, init))

        override val size: kotlin.Int
            get() = this.value.size
    }


    @JvmInline
    value class LongVector(override val value: LongArray) : Vector<LongArray> {
        constructor(size: kotlin.Int, init: (kotlin.Int) -> kotlin.Long = { 0L }) : this(LongArray(size, init))

        override val size: kotlin.Int
            get() = this.value.size
    }


    @JvmInline
    value class FloatVector(override val value: FloatArray) : Vector<FloatArray> {
        constructor(size: kotlin.Int, init: (kotlin.Int) -> kotlin.Float = { 0.0f }) : this(FloatArray(size, init))

        override val size: kotlin.Int
            get() = this.value.size
    }

    @JvmInline
    value class DoubleVector(override val value: DoubleArray) : Vector<DoubleArray> {
        constructor(size: kotlin.Int, init: (kotlin.Int) -> kotlin.Double = { 0.0 }) : this(DoubleArray(size, init))

        override val size: kotlin.Int
            get() = this.value.size
    }
}
