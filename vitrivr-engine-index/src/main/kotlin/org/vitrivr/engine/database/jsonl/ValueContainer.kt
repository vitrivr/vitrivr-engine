package org.vitrivr.engine.database.jsonl

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.serializer.DateSerializer
import org.vitrivr.engine.core.model.serializer.UUIDSerializer
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

@Serializable
sealed class ValueContainer(val type: Type) {

    companion object {
        fun fromValue(value: Value<*>): ValueContainer = when (value) {
            is Value.Boolean -> BooleanValueContainer(value.value)
            is Value.Byte -> ByteValueContainer(value.value)
            is Value.DateTime -> DateTimeValueContainer(value.value)
            is Value.Double -> DoubleValueContainer(value.value)
            is Value.Float -> FloatValueContainer(value.value)
            is Value.Int -> IntValueContainer(value.value)
            is Value.Long -> LongValueContainer(value.value)
            is Value.Short -> ShortValueContainer(value.value)
            is Value.String -> StringValueContainer(value.value)
            is Value.Text -> TextValueContainer(value.value)
            is Value.UUIDValue -> UuidValueContainer(value.value)
            is Value.BooleanVector -> BooleanVectorValueContainer(value.value)
            is Value.DoubleVector -> DoubleVectorValueContainer(value.value)
            is Value.FloatVector -> FloatVectorValueContainer(value.value)
            is Value.IntVector -> IntVectorValueContainer(value.value)
            is Value.LongVector -> LongVectorValueContainer(value.value)
        }
    }

    abstract fun toValue(): Value<*>

}

@Serializable
class BooleanValueContainer(private val value: Boolean) : ValueContainer(Type.Boolean) {
    override fun toValue(): Value<Boolean> = Value.Boolean(value)
}

@Serializable
class ByteValueContainer(private val value: Byte) : ValueContainer(Type.Byte) {
    override fun toValue(): Value<Byte> = Value.Byte(value)
}

@Serializable
class DateTimeValueContainer(@Serializable(DateSerializer::class) private val value: Date) :
    ValueContainer(Type.Datetime) {
    override fun toValue(): Value<Date> = Value.DateTime(value)
}

@Serializable
class DoubleValueContainer(private val value: Double) : ValueContainer(Type.Double) {
    override fun toValue(): Value<Double> = Value.Double(value)
}

@Serializable
class FloatValueContainer(private val value: Float) : ValueContainer(Type.Float) {
    override fun toValue(): Value<Float> = Value.Float(value)
}

@Serializable
class IntValueContainer(private val value: Int) : ValueContainer(Type.Int) {
    override fun toValue(): Value<Int> = Value.Int(value)
}

@Serializable
class LongValueContainer(private val value: Long) : ValueContainer(Type.Long) {
    override fun toValue(): Value<Long> = Value.Long(value)
}

@Serializable
class ShortValueContainer(private val value: Short) : ValueContainer(Type.Short) {
    override fun toValue(): Value<Short> = Value.Short(value)
}

@Serializable
class StringValueContainer(private val value: String) : ValueContainer(Type.String) {
    override fun toValue(): Value<String> = Value.String(value)
}

@Serializable
class TextValueContainer(private val value: String) : ValueContainer(Type.Text) {
    override fun toValue(): Value<String> = Value.Text(value)
}

@Serializable
class UuidValueContainer(@Serializable(UUIDSerializer::class) private val value: UUID) : ValueContainer(Type.UUID) {
    override fun toValue(): Value<UUID> = Value.UUIDValue(value)
}

@Serializable
class BooleanVectorValueContainer(private val value: BooleanArray) : ValueContainer(Type.BooleanVector(value.size)) {
    override fun toValue(): Value<BooleanArray> = Value.BooleanVector(value)
}

@Serializable
class DoubleVectorValueContainer(private val value: DoubleArray) : ValueContainer(Type.DoubleVector(value.size)) {
    override fun toValue(): Value<DoubleArray> = Value.DoubleVector(value)
}

@Serializable
class FloatVectorValueContainer(private val value: FloatArray) : ValueContainer(Type.FloatVector(value.size)) {
    override fun toValue(): Value<FloatArray> = Value.FloatVector(value)
}

@Serializable
class IntVectorValueContainer(private val value: IntArray) : ValueContainer(Type.IntVector(value.size)) {
    override fun toValue(): Value<IntArray> = Value.IntVector(value)
}

@Serializable
class LongVectorValueContainer(private val value: LongArray) : ValueContainer(Type.LongVector(value.size)) {
    override fun toValue(): Value<LongArray> = Value.LongVector(value)
}