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
    Type.Text -> Value.String(directory.getString(tagType))
    Type.UUID -> Value.UUIDValue(UUID.fromString(directory.getString(tagType)))
    is Type.BooleanVector -> throw IllegalArgumentException("Unsupported type: $type")
    is Type.DoubleVector -> throw IllegalArgumentException("Unsupported type: $type")
    is Type.FloatVector -> throw IllegalArgumentException("Unsupported type: $type")
    is Type.IntVector -> throw IllegalArgumentException("Unsupported type: $type")
    is Type.LongVector -> throw IllegalArgumentException("Unsupported type: $type")
}

private fun JsonElement.convertType(type: Type): Value<*>? {
    return when (this) {
        is JsonPrimitive -> {
            if (this.isString) {
                when (type) {
                    Type.String -> Value.String(this.content)
                    Type.Text -> Value.String(this.content)
                    Type.Datetime -> convertDate(this.content)?.let { Value.DateTime(it) }
                    else -> null
                }
            } else {
                when (type) {
                    Type.Boolean -> this.booleanOrNull?.let { Value.Boolean(it) }
                    Type.Byte -> this.intOrNull?.let { Value.Byte(it.toByte()) }
                    Type.Short -> this.intOrNull?.let { Value.Short(it.toShort()) }
                    Type.Int -> this.intOrNull?.let { Value.Int(it) }
                    Type.Long -> this.longOrNull?.let { Value.Long(it) }
                    Type.Float -> this.floatOrNull?.let { Value.Float(it) }
                    Type.Double -> this.doubleOrNull?.let { Value.Double(it) }
                    else -> null
                }
            }
        }
        is JsonObject, is JsonArray -> {
            when (type) {
                Type.Text -> Value.String(this.toString())
                Type.String -> Value.String(this.toString())
                Type.UUID -> Value.UUIDValue(UUID.fromString(this.toString()))
                else -> throw IllegalArgumentException("Cannot convert non-primitive JsonElement to type $type")
            }
        }
        else -> {
            throw IllegalStateException("Unsupported JsonElement type")
        }
    }
}

class ExifMetadataExtractor(input: Operator<Retrievable>, analyser: ExifMetadata, field: Schema.Field<ContentElement<*>, AnyMapStructDescriptor>?, parameters: Map<String,String>) : AbstractExtractor<ContentElement<*>, AnyMapStructDescriptor>(input, analyser, field, parameters) {


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
                    if (fullname in attributes){
                        columnValues[fullname] = Value.String(tag.description)
                    }
                    try {
                        val json = Json.parseToJsonElement(tag.description).jsonObject
                        json.forEach { (key, value) ->
                            attributes[key]?.let { attribute ->
                                value.convertType(attribute.type)?.let { converted ->
                                    columnValues[key] = converted
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
