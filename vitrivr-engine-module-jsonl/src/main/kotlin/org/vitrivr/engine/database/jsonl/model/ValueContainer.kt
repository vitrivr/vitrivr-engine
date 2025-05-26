package org.vitrivr.engine.database.jsonl.model

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.serializer.DateSerializer
import org.vitrivr.engine.core.model.serializer.UUIDSerializer
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
sealed class ValueContainer {

    companion object {
        fun fromValue(value: Value<*>): ValueContainer = when (value) {
            is Value.Boolean -> BooleanValueContainer(value.value)
            is Value.Byte -> ByteValueContainer(value.value)
            is Value.DateTime -> DateTimeValueContainer(value.value)
            is Value.LocalDateTimeValue -> LocalDateTimeValueContainer(value.value)
            is Value.Double -> DoubleValueContainer(value.value)
            is Value.Float -> FloatValueContainer(value.value)
            is Value.Int -> IntValueContainer(value.value)
            is Value.Long -> LongValueContainer(value.value)
            is Value.Short -> ShortValueContainer(value.value)
            is Value.String -> StringValueContainer(value.value)
            is Value.Text -> TextValueContainer(value.value)
            is Value.UUIDValue -> UuidValueContainer(value.value)
            is Value.GeographyValue -> GeographyValueContainer(value.wkt, value.srid)
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
class BooleanValueContainer(private val value: Boolean) : ValueContainer() {
    override fun toValue(): Value<Boolean> = Value.Boolean(value)
}

@Serializable
class ByteValueContainer(private val value: Byte) : ValueContainer() {
    override fun toValue(): Value<Byte> = Value.Byte(value)
}

@Serializable
class DateTimeValueContainer(@Serializable(DateSerializer::class) private val value: Date) :
    ValueContainer() {
    override fun toValue(): Value<Date> = Value.DateTime(value)
}

@Serializable
class LocalDateTimeValueContainer(
    @Serializable(LocalDateTimeSerializer::class) private val value: LocalDateTime
) : ValueContainer() {
    override fun toValue(): Value<LocalDateTime> = Value.LocalDateTimeValue(value)
}


@Serializable
class DoubleValueContainer(private val value: Double) : ValueContainer() {
    override fun toValue(): Value<Double> = Value.Double(value)
}

@Serializable
class FloatValueContainer(private val value: Float) : ValueContainer() {
    override fun toValue(): Value<Float> = Value.Float(value)
}

@Serializable
class IntValueContainer(private val value: Int) : ValueContainer() {
    override fun toValue(): Value<Int> = Value.Int(value)
}

@Serializable
class LongValueContainer(private val value: Long) : ValueContainer() {
    override fun toValue(): Value<Long> = Value.Long(value)
}

@Serializable
class ShortValueContainer(private val value: Short) : ValueContainer() {
    override fun toValue(): Value<Short> = Value.Short(value)
}

@Serializable
class StringValueContainer(private val value: String) : ValueContainer() {
    override fun toValue(): Value<String> = Value.String(value)
}

@Serializable
class TextValueContainer(private val value: String) : ValueContainer() {
    override fun toValue(): Value<String> = Value.Text(value)
}

@Serializable
class UuidValueContainer(@Serializable(UUIDSerializer::class) private val value: UUID) : ValueContainer() {
    override fun toValue(): Value<UUID> = Value.UUIDValue(value)
}

@Serializable
class GeographyValueContainer(private val wkt: String, private val srid: Int) : ValueContainer() {
    override fun toValue(): Value<String> {
        return Value.GeographyValue(wkt, srid)
    }
}

@Serializable
class BooleanVectorValueContainer(private val value: BooleanArray) : ValueContainer() {
    override fun toValue(): Value<BooleanArray> = Value.BooleanVector(value)
}

@Serializable
class DoubleVectorValueContainer(private val value: DoubleArray) : ValueContainer() {
    override fun toValue(): Value<DoubleArray> = Value.DoubleVector(value)
}

@Serializable
class FloatVectorValueContainer(private val value: FloatArray) : ValueContainer() {
    override fun toValue(): Value<FloatArray> = Value.FloatVector(value)
}

@Serializable
class IntVectorValueContainer(private val value: IntArray) : ValueContainer() {
    override fun toValue(): Value<IntArray> = Value.IntVector(value)
}

@Serializable
class LongVectorValueContainer(private val value: LongArray) : ValueContainer() {
    override fun toValue(): Value<LongArray> = Value.LongVector(value)
}