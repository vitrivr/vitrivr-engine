package org.vitrivr.engine.core.features.metadata.source.exif

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Directory
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.file.FileSource
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

val logger: KLogger = KotlinLogging.logger {}

private val NON_ALPHANUMERIC_REGEX = Regex("[^a-zA-Z0-9]")

private val DATE_FORMAT_PATTERNS = listOf(
    "yyyy:MM:dd HH:mm:ss",
    "yyyy-MM-dd HH:mm:ss",
    "dd.MM.yyyy HH:mm:ss",
    "MM/dd/yyyy HH:mm:ss"
)

private fun convertDate(date: String): Date? {
    for (pattern in DATE_FORMAT_PATTERNS) {
        try {
            return SimpleDateFormat(pattern).parse(date)
        } catch (e: ParseException) {
        }
    }
    logger.warn { "Failed to parse date: $date" }
    return null
}

fun JsonElement.toValue(): Value<*>? {
    return when (this) {
        is JsonPrimitive -> {
            when {
                this.booleanOrNull != null -> Value.Boolean(this.boolean)
                this.intOrNull != null -> Value.Int(this.int)
                this.longOrNull != null -> Value.Long(this.long)
                this.floatOrNull != null -> Value.Float(this.float)
                this.doubleOrNull != null -> Value.Double(this.double)
                this.isString -> Value.String(this.content)  // Only isString exists
                else -> null
            }
        }
        is JsonArray, is JsonObject -> Value.String(this.toString())
        else -> null
    }
}

fun Value<*>.convertToType(type: Type): Value<*>? {
    return when (type) {
        Type.Boolean -> if (this is Value.Boolean) this else null
        Type.Byte -> if (this is Value.Int) Value.Byte(this.value.toByte()) else null
        Type.Short -> if (this is Value.Int) Value.Short(this.value.toShort()) else null
        Type.Int -> if (this is Value.Int) this else null
        Type.Long -> if (this is Value.Long) this else null
        Type.Float -> if (this is Value.Float) this else null
        Type.Double -> if (this is Value.Double) this else null
        Type.String -> if (this is Value.String) this else null

        Type.Text -> if (this is Value.String) {
            Value.Text(this.value)
        } else null

        Type.Datetime -> if (this is Value.String) {
            convertDate(this.value)?.let { Value.DateTime(it) }
        } else null

        Type.UUID -> if (this is Value.String) {
            Value.UUIDValue(UUID.fromString(this.value))
        } else null

        else -> null
    }
}

class ExifMetadataExtractor : AbstractExtractor<ContentElement<*>, AnyMapStructDescriptor> {

    constructor(input: Operator<Retrievable>, analyser: ExifMetadata, field: Schema.Field<ContentElement<*>, AnyMapStructDescriptor>) : super(input, analyser, field)
    constructor(input: Operator<Retrievable>, analyser: ExifMetadata, name: String): super(input, analyser, name)

    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.filteredAttribute(SourceAttribute::class.java)?.source is FileSource

    override fun extract(retrievable: Retrievable): List<AnyMapStructDescriptor> {
        val metadata = ImageMetadataReader.readMetadata((retrievable.filteredAttribute(SourceAttribute::class.java)?.source as FileSource).path.toFile())
        val columnValues = mutableMapOf<AttributeName, Value<*>>()

        val attributes = this.field?.parameters?.map { (k, v) -> k to Attribute(k, Type.valueOf(v)) }?.toMap() ?: emptyMap()

        for (directory in metadata.directories) {
            for (tag in directory.tags) {
                val tagname = tag.tagName.replace(NON_ALPHANUMERIC_REGEX, "")
                val fullname = "${directory.name.replace(NON_ALPHANUMERIC_REGEX, "")}_$tagname"

                if (fullname == "ExifSubIFD_UserComment" || fullname == "JpegComment_JPEGComment") {
                    if (fullname in attributes) {
                        columnValues[fullname] = Value.String(tag.description)
                    }
                    try {
                        val jsonElement = Json.parseToJsonElement(tag.description)
                        if (jsonElement is JsonObject) {
                            attributes.forEach { (attributeKey, attributeValue) ->
                                if (jsonElement.containsKey(attributeKey)) {
                                    val jsonValue = jsonElement[attributeKey]?.toValue()

                                    jsonValue?.convertToType(attributeValue.type)?.let { converted ->
                                        columnValues[attributeKey] = converted
                                    }
                                }
                            }
                        }
                    } catch (e: SerializationException) {
                        logger.warn { "Failed to parse JSON from $fullname: ${tag.description}" }
                    }
                } else {
                    attributes[fullname]?.let { attribute ->
                        convertType(directory, tag.tagType, attribute.type)?.let { converted ->
                            columnValues[fullname] = converted
                        }
                    }
                }
            }
        }
        logger.info { "Extracted fields: ${columnValues.entries.joinToString { (key, value) -> "$key = ${value.value}" }}" }

        return listOf(AnyMapStructDescriptor(UUID.randomUUID(), retrievable.id, attributes.values.toList(), columnValues.mapValues { it.value }, field = this.field))
    }
}

private fun convertType(directory: Directory, tagType: Int, type: Type): Value<*>? = when (type) {
    Type.Boolean -> Value.Boolean(directory.getBoolean(tagType))
    Type.Byte -> Value.Byte(directory.getObject(tagType) as Byte)
    Type.Datetime -> convertDate(directory.getString(tagType))?.let { Value.DateTime(it) }
    Type.Double -> Value.Double(directory.getDouble(tagType))
    Type.Float -> Value.Float(directory.getFloat(tagType))
    Type.Int -> Value.Int(directory.getInt(tagType))
    Type.Long -> Value.Long(directory.getLong(tagType))
    Type.Short -> Value.Short(directory.getObject(tagType) as Short)
    Type.String -> Value.String(directory.getString(tagType))
    Type.Text -> Value.Text(directory.getString(tagType))  // Ensure Type.Text returns Value.Text
    Type.UUID -> Value.UUIDValue(UUID.fromString(directory.getString(tagType)))
    is Type.BooleanVector, is Type.DoubleVector, is Type.FloatVector, is Type.IntVector, is Type.LongVector -> throw IllegalArgumentException("Unsupported type: $type")
}
