package org.vitrivr.engine.core.features.metadata.source.exif

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Directory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.float
import kotlinx.serialization.json.double
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.*
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.struct.MapStructDescriptor
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
    is Type.BooleanVector -> throw IllegalArgumentException("Unsupported type: $type")
    is Type.DoubleVector -> throw IllegalArgumentException("Unsupported type: $type")
    is Type.FloatVector -> throw IllegalArgumentException("Unsupported type: $type")
    is Type.IntVector -> throw IllegalArgumentException("Unsupported type: $type")
    is Type.LongVector -> throw IllegalArgumentException("Unsupported type: $type")
}

private fun JsonObject.convertType(type: Type): Value<*>? {
    val jsonPrimitive = this.jsonPrimitive
    if (jsonPrimitive.isString) {
        return when (type) {
            Type.String -> Value.String(jsonPrimitive.content)
            Type.Datetime -> convertDate(jsonPrimitive.content)?.let { Value.DateTime(it) }
            else -> null
        }
    } else {
        return when (type) {
            Type.Boolean -> Value.Boolean(jsonPrimitive.boolean)
            Type.Byte -> Value.Byte(jsonPrimitive.int.toByte())
            Type.Short -> Value.Short(jsonPrimitive.int.toShort())
            Type.Int -> Value.Int(jsonPrimitive.int)
            Type.Long -> Value.Long(jsonPrimitive.int.toLong())
            Type.Float -> Value.Float(jsonPrimitive.float)
            Type.Double -> Value.Double(jsonPrimitive.double)
            else -> null
        }
    }
}

class ExifMetadataExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<ContentElement<*>, MapStructDescriptor>?
) : AbstractExtractor<ContentElement<*>, MapStructDescriptor>(input, field) {


    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.filteredAttribute(SourceAttribute::class.java)?.source is FileSource

    override fun extract(retrievable: Retrievable): List<MapStructDescriptor> {
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
                                value.jsonObject.convertType(attribute.type)?.let { converted ->
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

        return listOf(MapStructDescriptor(UUID.randomUUID(), retrievable.id, attributes.values.toList(), columnValues.mapValues { it.value }, field = this.field))
    }
}
