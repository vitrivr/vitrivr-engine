package org.vitrivr.engine.core.model.types

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.serializer.DateTimeSerializer
import java.util.*

/**
 * A [Value] in vitrivr-engine maps primitive data types.
 *
 * Part of the vitrivr-engine's internal type system.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
@Serializable()
sealed interface Value<T> {

    companion object {
        /**
         * Converts [Any] value to a [Value].
         *
         * @param value The value to convert.
         * @return [Value]
         */
        fun of(value: Any): Value<*> = when (value) {
            is kotlin.String -> String(value)
            is kotlin.Boolean -> Boolean(value)
            is kotlin.Byte -> Byte(value)
            is kotlin.Short -> Short(value)
            is kotlin.Int -> Int(value)
            is kotlin.Long -> Long(value)
            is kotlin.Float -> Float(value)
            is kotlin.Double -> Double(value)
            is BooleanArray -> BooleanVector(value)
            is DoubleArray -> DoubleVector(value)
            is FloatArray -> FloatVector(value)
            is LongArray -> LongVector(value)
            is IntArray -> IntVector(value)
            is Date -> DateTime(value)
            is UUID -> UUIDValue(value)
            else -> throw IllegalArgumentException("Unsupported data type.")
        }
    }

    /** The actual, primitive value. */
    val value: T

    /** Reference to the [Type] of the Value*/
    val type: Type


    sealed interface ScalarValue<T> : Value<T>, Comparable<ScalarValue<T>>

    @JvmInline
    @Serializable
    value class String(override val value: kotlin.String) : ScalarValue<kotlin.String> {
        override val type: Type
            get() = Type.String
        override fun compareTo(other: ScalarValue<kotlin.String>) = this.value.compareTo(other.value)
    }

    @JvmInline
    @Serializable
    value class Text(override val value: kotlin.String) : ScalarValue<kotlin.String> {
        override val type: Type
            get() = Type.Text
        override fun compareTo(other: ScalarValue<kotlin.String>) = this.value.compareTo(other.value)
    }

    @JvmInline
    @Serializable
    value class Boolean(override val value: kotlin.Boolean) : ScalarValue<kotlin.Boolean> {
        override val type: Type
            get() = Type.Boolean
        override fun compareTo(other: ScalarValue<kotlin.Boolean>) = this.value.compareTo(other.value)
    }

    @JvmInline
    @Serializable
    value class Byte(override val value: kotlin.Byte) : ScalarValue<kotlin.Byte> {
        override val type: Type
            get() = Type.Byte
        override fun compareTo(other: ScalarValue<kotlin.Byte>) = this.value.compareTo(other.value)
    }

    @JvmInline
    @Serializable
    value class Short(override val value: kotlin.Short) : ScalarValue<kotlin.Short> {
        override val type: Type
            get() = Type.Short
        override fun compareTo(other: ScalarValue<kotlin.Short>) = this.value.compareTo(other.value)
    }

    @JvmInline
    @Serializable
    value class Int(override val value: kotlin.Int) : ScalarValue<kotlin.Int> {
        override val type: Type
            get() = Type.Int
        override fun compareTo(other: ScalarValue<kotlin.Int>) = this.value.compareTo(other.value)
    }

    @JvmInline
    @Serializable
    value class Long(override val value: kotlin.Long) : ScalarValue<kotlin.Long> {
        override val type: Type
            get() = Type.Long
        override fun compareTo(other: ScalarValue<kotlin.Long>) = this.value.compareTo(other.value)
    }

    @JvmInline
    @Serializable
    value class Float(override val value: kotlin.Float) : ScalarValue<kotlin.Float> {
        override val type: Type
            get() = Type.Float
        override fun compareTo(other: ScalarValue<kotlin.Float>) = this.value.compareTo(other.value)
    }

    @JvmInline
    @Serializable
    value class Double(override val value: kotlin.Double) : ScalarValue<kotlin.Double> {
        override val type: Type
            get() = Type.Double
        override fun compareTo(other: ScalarValue<kotlin.Double>) = this.value.compareTo(other.value)
    }

    @JvmInline
    @Serializable(with = DateTimeSerializer::class)
    value class DateTime(override val value: Date) : ScalarValue<Date> {
        override val type: Type
            get() = Type.Datetime
        override fun compareTo(other: ScalarValue<Date>) = this.value.compareTo(other.value)
    }

    @JvmInline
    value class UUIDValue(override val value: UUID) : ScalarValue<UUID> {
        override val type: Type
            get() = Type.UUID
        override fun compareTo(other: ScalarValue<UUID>) = this.value.compareTo(other.value)
    }

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
    @Serializable
    sealed interface Vector<T> : Value<T> {
        val size: kotlin.Int
    }


    @JvmInline
    @Serializable
    value class BooleanVector(override val value: BooleanArray) : Vector<BooleanArray> {
        constructor(size: kotlin.Int, init: (kotlin.Int) -> kotlin.Boolean = { false }) : this(BooleanArray(size, init))

        override val size: kotlin.Int
            get() = this.value.size

        override val type: Type
            get() = Type.BooleanVector(size)
    }

    @JvmInline
    @Serializable
    value class IntVector(override val value: IntArray) : Vector<IntArray> {
        constructor(size: kotlin.Int, init: (kotlin.Int) -> kotlin.Int = { 0 }) : this(IntArray(size, init))

        override val size: kotlin.Int
            get() = this.value.size

        override val type: Type
            get() = Type.IntVector(size)
    }


    @JvmInline
    @Serializable
    value class LongVector(override val value: LongArray) : Vector<LongArray> {
        constructor(size: kotlin.Int, init: (kotlin.Int) -> kotlin.Long = { 0L }) : this(LongArray(size, init))

        override val size: kotlin.Int
            get() = this.value.size

        override val type: Type
            get() = Type.LongVector(size)
    }


    @JvmInline
    @Serializable
    value class FloatVector(override val value: FloatArray) : Vector<FloatArray> {
        constructor(size: kotlin.Int, init: (kotlin.Int) -> kotlin.Float = { 0.0f }) : this(FloatArray(size, init))

        override val size: kotlin.Int
            get() = this.value.size

        override val type: Type
            get() = Type.FloatVector(size)
    }

    @JvmInline
    @Serializable
    value class DoubleVector(override val value: DoubleArray) : Vector<DoubleArray> {
        constructor(size: kotlin.Int, init: (kotlin.Int) -> kotlin.Double = { 0.0 }) : this(DoubleArray(size, init))

        override val size: kotlin.Int
            get() = this.value.size

        override val type: Type
            get() = Type.DoubleVector(size)
    }
}
