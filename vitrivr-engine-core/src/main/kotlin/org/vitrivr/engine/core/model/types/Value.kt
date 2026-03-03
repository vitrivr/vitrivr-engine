package org.vitrivr.engine.core.model.types

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonPrimitive
import org.vitrivr.engine.core.model.serializer.DateTimeSerializer
import java.util.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.*

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
    @Serializable(with = FloatVectorAsArraySerializer::class)
    value class FloatVector(override val value: FloatArray) : Vector<FloatArray> {
        constructor(size: kotlin.Int, init: (kotlin.Int) -> kotlin.Float = { 0.0f }) : this(FloatArray(size, init))

        override val size: kotlin.Int
            get() = this.value.size

        override val type: Type
            get() = Type.FloatVector(size)
    }

    /**
     * Forces Value.FloatVector to encode/decode its FloatArray strictly as a JSON array.
     */
    object FloatVectorAsArraySerializer : KSerializer<FloatVector> {
        private val delegate = FloatArraySerializer()

        override val descriptor = delegate.descriptor

        override fun serialize(encoder: Encoder, value: FloatVector) {
            delegate.serialize(encoder, value.value)
        }

        override fun deserialize(decoder: Decoder): FloatVector {
            val arr = delegate.deserialize(decoder)
            return FloatVector(arr)
        }
    }

    /**
     * Untagged / JSON-only float vector wrapper.
     *
     * Serializes strictly as: [0.1, 0.2, ...]  (NO type discriminator)
     *
     * Use this when a field is typed as Any / polymorphic and you must avoid:
     *   { "type": "kotlin.FloatArray", ... }
     */
    @Serializable(with = UntaggedFloatVectorSerializer::class)
    @SerialName("UntaggedFloatVector")
    data class UntaggedFloatVector(val values: FloatArray)

    fun Value.FloatVector.asUntagged(): UntaggedFloatVector = UntaggedFloatVector(this.value)

    object UntaggedFloatVectorSerializer : KSerializer<UntaggedFloatVector> {
        override val descriptor = FloatArraySerializer().descriptor

        override fun serialize(encoder: Encoder, value: UntaggedFloatVector) {
            val jsonEncoder = encoder as? JsonEncoder
                ?: error("UntaggedFloatVectorSerializer supports JSON only.")
            val arr = JsonArray(value.values.map { JsonPrimitive(it) })
            jsonEncoder.encodeJsonElement(arr)
        }

        override fun deserialize(decoder: Decoder): UntaggedFloatVector {
            val jsonDecoder = decoder as? JsonDecoder
                ?: error("UntaggedFloatVectorSerializer supports JSON only.")
            val el = jsonDecoder.decodeJsonElement()

            val arr = el as? JsonArray
                ?: error("Expected JSON array for UntaggedFloatVector, got: $el")

            val floats = FloatArray(arr.size) { i ->
                arr[i].jsonPrimitive.float
            }
            return UntaggedFloatVector(floats)
        }
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

/**
 * Serializes Value<*> as "content only" (no polymorphic type discriminator):
 * - scalars -> JSON primitive
 * - vectors -> JSON array
 */
object ValueContentOnlySerializer : KSerializer<Value<*>> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("ValueContentOnly", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Value<*>) {
        // IMPORTANT: delegate to the concrete subtype serializer (non-polymorphic!)
        when (value) {
            is Value.String -> encoder.encodeSerializableValue(Value.String.serializer(), value)
            is Value.Text -> encoder.encodeSerializableValue(Value.Text.serializer(), value)
            is Value.Boolean -> encoder.encodeSerializableValue(Value.Boolean.serializer(), value)
            is Value.Byte -> encoder.encodeSerializableValue(Value.Byte.serializer(), value)
            is Value.Short -> encoder.encodeSerializableValue(Value.Short.serializer(), value)
            is Value.Int -> encoder.encodeSerializableValue(Value.Int.serializer(), value)
            is Value.Long -> encoder.encodeSerializableValue(Value.Long.serializer(), value)
            is Value.Float -> encoder.encodeSerializableValue(Value.Float.serializer(), value)
            is Value.Double -> encoder.encodeSerializableValue(Value.Double.serializer(), value)
            is Value.DateTime -> encoder.encodeSerializableValue(Value.DateTime.serializer(), value)

            is Value.BooleanVector -> encoder.encodeSerializableValue(Value.BooleanVector.serializer(), value)
            is Value.IntVector -> encoder.encodeSerializableValue(Value.IntVector.serializer(), value)
            is Value.LongVector -> encoder.encodeSerializableValue(Value.LongVector.serializer(), value)
            is Value.FloatVector -> encoder.encodeSerializableValue(Value.FloatVector.serializer(), value) // uses your array serializer
            is Value.DoubleVector -> encoder.encodeSerializableValue(Value.DoubleVector.serializer(), value)

            // UUIDValue currently isn't @Serializable in your file; if you need it, either add @Serializable
            // and handle it here, or encode it as string:
            is Value.UUIDValue -> {
                val jsonEncoder = encoder as? JsonEncoder
                    ?: error("UUIDValue content-only serialization supports JSON only.")
                jsonEncoder.encodeJsonElement(JsonPrimitive(value.value.toString()))
            }

            else -> error("Unsupported Value subtype: ${value::class.qualifiedName}")
        }
    }

    override fun deserialize(decoder: Decoder): Value<*> {
        // Heuristic decode (only needed if you ever decode descriptors back)
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("ValueContentOnlySerializer supports JSON only.")
        val el = jsonDecoder.decodeJsonElement()

        return when (el) {
            is JsonArray -> {
                // default to FloatVector (you can choose DoubleVector if you prefer)
                val arr = FloatArray(el.size) { i -> el[i].jsonPrimitive.float }
                Value.FloatVector(arr)
            }
            is JsonPrimitive -> {
                when {
                    el.isString -> Value.String(el.content)
                    el.booleanOrNull != null -> Value.Boolean(el.boolean)
                    el.longOrNull != null -> Value.Long(el.long)
                    el.doubleOrNull != null -> Value.Double(el.double)
                    else -> Value.String(el.content)
                }
            }
            else -> error("Unsupported JSON for ValueContentOnlySerializer: $el")
        }
    }
}

/** Serializer for Map<String, Value<*>> where values are encoded without type discriminator. */
object DescriptorMapContentOnlySerializer : KSerializer<Map<String, Value<*>>> {
    private val delegate = MapSerializer(String.serializer(), ValueContentOnlySerializer)
    override val descriptor: SerialDescriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: Map<String, Value<*>>) = delegate.serialize(encoder, value)
    override fun deserialize(decoder: Decoder): Map<String, Value<*>> = delegate.deserialize(decoder)
}

