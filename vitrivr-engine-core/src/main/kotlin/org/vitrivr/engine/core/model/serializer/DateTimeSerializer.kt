package org.vitrivr.engine.core.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.vitrivr.engine.core.model.types.Value
import java.util.*

object DateTimeSerializer: KSerializer<Value.DateTime> {
    override val descriptor = PrimitiveSerialDescriptor("DateTime", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Value.DateTime) = encoder.encodeLong(value.value.time)
    override fun deserialize(decoder: Decoder): Value.DateTime = Value.DateTime(Date(decoder.decodeLong()))
}