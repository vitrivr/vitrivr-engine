package org.vitrivr.engine.core.model.types

import kotlinx.serialization.Serializable
import java.util.*

/**
 * The [Type]s supported by vitrivr-engine.
 *
 * @author Ralph Gasser
 * @author Loris Sauter
 * @version 2.0.0
 */
@Serializable
sealed interface Type {

    companion object {
        /**
         * Returns the [Type] for the given [String] representation.
         *
         * @param type [String] representation of the [Type]
         * @return [Type]
         */
        fun valueOf(type: kotlin.String, dimensions: kotlin.Int = 0): Type {
            return when (type.uppercase()) {
                "TEXT" -> Text
                "STRING" -> String
                "BOOLEAN" -> Boolean
                "BYTE" -> Byte
                "SHORT" -> Short
                "INT" -> Int
                "LONG" -> Long
                "FLOAT" -> Float
                "DOUBLE" -> Double
                "DATETIME" -> Datetime
                "BOOLEANVECTOR" -> BooleanVector(dimensions)
                "INTVECTOR" -> IntVector(dimensions)
                "LONGVECTOR" -> LongVector(dimensions)
                "FLOATVECTOR" -> FloatVector(dimensions)
                "DOUBLEVECTOR" -> DoubleVector(dimensions)
                else -> throw IllegalArgumentException("Type $type is not supported!")
            }
        }
    }


    /** The number of dimensions for this [Type]. */
    val dimensions: kotlin.Int


    /**
     *
     */
    fun defaultValue(): Value<*>

    /**
     * A [Type] that represents a (longer) text.
     *
     * Primarily needed, since simple strings and longer texts are treated differently by certain databases.
     */
    @Serializable
    data object Text : Type {
        override val dimensions: kotlin.Int = 1
        override fun defaultValue(): Value<*> = Value.String("")
    }

    /**
     * A [Type] that represents a simple String.
     *
     * Primarily needed, since simple strings and longer texts are treated differently by certain databases.
     */
    @Serializable
    data object String : Type {
        override val dimensions: kotlin.Int = 1
        override fun defaultValue(): Value<*> = Value.String("")
    }

    /**
     * A [Type] that represents a [Boolean] value.
     */
    @Serializable
    data object Boolean : Type {
        override val dimensions: kotlin.Int = 1
        override fun defaultValue(): Value<*> = Value.Boolean(false)
    }

    /**
     * A [Type] that represents a [Byte] value.
     */
    @Serializable
    data object Byte : Type {
        override val dimensions: kotlin.Int = 1
        override fun defaultValue(): Value<*> = Value.Byte(0)
    }

    /**
     * A [Type] that represents a [Short] value.
     */
    @Serializable
    data object Short : Type {
        override val dimensions: kotlin.Int = 1
        override fun defaultValue(): Value<*> = Value.Short(0)
    }

    /**
     * A [Type] that represents a [Int] value.
     */
    @Serializable
    data object Int : Type {
        override val dimensions: kotlin.Int = 1
        override fun defaultValue(): Value<*> = Value.Int(0)
    }

    /**
     * A [Type] that represents a [Long] value.
     */
    @Serializable
    data object Long : Type {
        override val dimensions: kotlin.Int = 1
        override fun defaultValue(): Value<*> = Value.Long(0L)
    }

    /**
     * A [Type] that represents a [Float] value.
     */
    @Serializable
    data object Float : Type {
        override val dimensions: kotlin.Int = 1
        override fun defaultValue(): Value<*> = Value.Float(0.0f)
    }

    /**
     * A [Type] that represents a [Double] value.
     */
    @Serializable
    data object Double : Type {
        override val dimensions: kotlin.Int = 1
        override fun defaultValue(): Value<*> = Value.Double(0.0)
    }

    /**
     * A [Type] that represents a [Datetime] value.
     */
    @Serializable
    data object Datetime : Type {
        override val dimensions: kotlin.Int = 1
        override fun defaultValue(): Value<*> = Value.DateTime(Date())
    }

    /**
     * A [Type] that represents a [UUID] value.
     */
    @Serializable
    data object UUID : Type {
        override val dimensions: kotlin.Int = 1
        override fun defaultValue(): Value<*> = Value.UUIDValue(java.util.UUID(0L, 0L))
    }

    /**
     * A [Type] that represents a [BooleanVector] value.
     */
    @Serializable
    data class BooleanVector(override val dimensions: kotlin.Int) : Type {
        override fun defaultValue(): Value<*> = Value.BooleanVector(this.dimensions)
    }

    /**
     * A [Type] that represents a [IntVector] value.
     */
    @Serializable
    data class IntVector(override val dimensions: kotlin.Int) : Type {
        override fun defaultValue(): Value<*> = Value.IntVector(this.dimensions)
    }

    /**
     * A [Type] that represents a [LongVector] value.
     */
    @Serializable
    data class LongVector(override val dimensions: kotlin.Int) : Type {
        override fun defaultValue(): Value<*> = Value.LongVector(this.dimensions)
    }

    /**
     * A [Type] that represents a [FloatVector] value.
     */
    @Serializable
    data class FloatVector(override val dimensions: kotlin.Int) : Type {
        override fun defaultValue(): Value<*> = Value.FloatVector(this.dimensions)
    }

    /**
     * A [Type] that represents a [DoubleVector] value.
     */
    @Serializable
    data class DoubleVector(override val dimensions: kotlin.Int) : Type {
        override fun defaultValue(): Value<*> = Value.DoubleVector(this.dimensions)
    }
}
