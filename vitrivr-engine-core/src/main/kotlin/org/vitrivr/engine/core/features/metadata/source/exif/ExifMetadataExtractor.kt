package org.vitrivr.engine.core.features.metadata.source.exif

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Directory
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.MapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.file.FileSource
import java.text.SimpleDateFormat
import java.util.*

private const val DATE_FORMAT_PATTERN = "yyyy:MM:dd HH:mm:ss"
private val NON_ALPHANUMERIC_REGEX = Regex("[^a-zA-Z0-9]")

private fun convertDate(date: String): Date =
    SimpleDateFormat(DATE_FORMAT_PATTERN).parse(date)

private fun convertType(directory: Directory, tagType: Int, type: Type): Value<*> =
    when (type) {
        Type.STRING -> Value.String(directory.getString(tagType))
        Type.BOOLEAN -> Value.Boolean(directory.getBoolean(tagType))
        Type.BYTE -> Value.Byte(directory.getObject(tagType) as Byte)
        Type.SHORT -> Value.Short(directory.getObject(tagType) as Short)
        Type.INT -> Value.Int(directory.getInt(tagType))
        Type.LONG -> Value.Long(directory.getLong(tagType))
        Type.FLOAT -> Value.Float(directory.getFloat(tagType))
        Type.DOUBLE -> Value.Double(directory.getDouble(tagType))
        Type.DATETIME -> Value.DateTime(convertDate(directory.getString(tagType)))
        else -> throw IllegalArgumentException("Unsupported type: $type")
    }

private fun JsonElement.convertType(type: Type): Value<*> =
    when (type) {
        Type.STRING -> Value.String(this.asString)
        Type.BOOLEAN -> Value.Boolean(this.asBoolean)
        Type.BYTE -> Value.Byte(this.asByte)
        Type.SHORT -> Value.Short(this.asShort)
        Type.INT -> Value.Int(this.asInt)
        Type.LONG -> Value.Long(this.asLong)
        Type.FLOAT -> Value.Float(this.asFloat)
        Type.DOUBLE -> Value.Double(this.asDouble)
        Type.DATETIME -> Value.DateTime(convertDate(this.asString))
        else -> throw IllegalArgumentException("Unsupported type: $type")
    }

class ExifMetadataExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<ContentElement<*>, MapStructDescriptor>,
    persisting: Boolean = true
) : AbstractExtractor<ContentElement<*>, MapStructDescriptor>(input, field, persisting) {

    private val logger: KLogger = KotlinLogging.logger {}

    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.filteredAttribute(SourceAttribute::class.java)?.source is FileSource

    override fun extract(retrievable: Retrievable): List<MapStructDescriptor> {
        val metadata = ImageMetadataReader.readMetadata((retrievable.filteredAttribute(SourceAttribute::class.java)?.source as FileSource).path.toFile())
        val columnValues = mutableMapOf<String, Value<*>>()

        for (directory in metadata.directories) {
            for (tag in directory.tags) {
                val tagname = tag.tagName.replace(NON_ALPHANUMERIC_REGEX, "")
                val fullname = "${directory.name.replace(NON_ALPHANUMERIC_REGEX, "")}_$tagname"

                if (fullname == "ExifSubIFD_UserComment") {
                    if (fullname in this.field.parameters){
                        columnValues[fullname] = Value.String(tag.description)
                    }
                    try {
                        val json = JsonParser.parseString(tag.description).asJsonObject
                        json.entrySet().forEach { (key, value) ->
                            this.field.parameters[key]?.let {
                                columnValues[key] = value.convertType(Type.valueOf(it))
                            }
                        }
                    } catch (e: JsonParseException) {
                        logger.warn { "Failed to parse JSON from UserComment: ${tag.description}" }
                        continue
                    }
                } else {
                    this.field.parameters[fullname]?.let {
                        val type = Type.valueOf(it)
                        columnValues[fullname] = convertType(directory, tag.tagType, type)
                    }
                }
            }
        }
        logger.info { "Extracted fields: ${columnValues.entries.joinToString { (key, value) -> "$key = ${value.value}" }}" }

        return listOf(MapStructDescriptor(UUID.randomUUID(), retrievable.id, this.field.parameters, columnValues.mapValues { it.value.value }))
    }
}
