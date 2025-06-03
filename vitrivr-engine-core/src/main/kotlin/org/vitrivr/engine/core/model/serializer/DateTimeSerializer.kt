package org.vitrivr.engine.core.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.vitrivr.engine.core.model.types.Value
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


    object DateTimeSerializer : KSerializer<LocalDateTime> { // Value.DateTime now wraps LocalDateTime

        override val descriptor = PrimitiveSerialDescriptor("DateTime", PrimitiveKind.LONG)

        override fun serialize(encoder: Encoder, value: LocalDateTime) {
            // Convert LocalDateTime to epoch milliseconds using UTC as the reference.
            val epochMilli = value.toInstant(ZoneOffset.UTC).toEpochMilli()
            encoder.encodeLong(epochMilli)
        }

        override fun deserialize(decoder: Decoder): LocalDateTime {
            val epochMilli = decoder.decodeLong()
            // Convert epoch milliseconds back to LocalDateTime, assuming the milliseconds were UTC-based.
            val localDateTime = Instant.ofEpochMilli(epochMilli).atZone(ZoneOffset.UTC).toLocalDateTime()
            return localDateTime
        }
    }