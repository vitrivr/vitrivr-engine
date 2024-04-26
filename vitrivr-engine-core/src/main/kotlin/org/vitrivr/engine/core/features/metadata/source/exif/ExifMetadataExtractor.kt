package org.vitrivr.engine.core.features.metadata.source.exif

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Directory
import com.drew.metadata.Metadata
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.MapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.FileSource
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


fun parseJson(jsonString: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    val regex = """"([^"]*)"\s*:\s*"([^"]*)"""".toRegex()

    regex.findAll(jsonString).forEach { matchResult ->
        val (key, value) = matchResult.destructured
        map[key] = value
    }

    return map
}

fun convertType(directory: Directory, tagType: Int, type: String): Value<*> {
    return when(type) {
        "STRING" -> Value.String(directory.getString(tagType))
        "BOOLEAN" -> Value.Boolean(directory.getBoolean(tagType))
        "BYTE" -> Value.Byte(directory.getObject(tagType) as Byte)
        "SHORT" -> Value.Short(directory.getObject(tagType) as Short)
        "INT" -> Value.Int(directory.getInt(tagType))
        "LONG" -> Value.Long(directory.getLong(tagType))
        "FLOAT" -> Value.Float(directory.getFloat(tagType))
        "DOUBLE" -> Value.Double(directory.getDouble(tagType))
        "DATETIME" -> Value.DateTime(SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(directory.getString(tagType)))
        else -> throw(IllegalArgumentException("Type not supported"))
    }
}

class ExifMetadataExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<ContentElement<*>, MapStructDescriptor>,
    persisting: Boolean = true
) : AbstractExtractor<ContentElement<*>, MapStructDescriptor>(input, field, persisting, bufferSize = 1) {

    val logger: KLogger = KotlinLogging.logger {}

    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.filteredAttribute(SourceAttribute::class.java)?.source is FileSource /* && retrievable.type is MediaType.IMAGE */


    override fun extract(retrievable: Retrievable): List<MapStructDescriptor> {
        var metadata = ImageMetadataReader.readMetadata((retrievable.filteredAttribute(SourceAttribute::class.java)?.source as FileSource).path.toFile())

        var columnValues = mutableMapOf<String, Value<*>>()

        for (directory in metadata.directories) {
            for (tag in directory.tags) {
                val tagname = tag.tagName.replace(Regex("[^a-zA-Z0-9]"), "")
                val fullname = "${directory.name.replace(Regex("[^a-zA-Z0-9]"), "")}_${tagname}"

                if (fullname == "ExifSubIFD_UserComment") {
                    val userComment = tag.description
                    if (userComment.isNotEmpty()) {
                        val jsonMap = parseJson(userComment)
                        for ((key, value) in jsonMap) {
                            if (this.field.parameters.containsKey(key)){
                                when (this.field.parameters[key]) {
                                    "STRING" -> columnValues[key] = Value.String(value)
                                    "BOOLEAN" -> columnValues[key] = Value.Boolean(value.toBoolean())
                                    "BYTE" -> columnValues[key] = Value.Byte(value.toByte())
                                    "SHORT" -> columnValues[key] = Value.Short(value.toShort())
                                    "INT" -> columnValues[key] = Value.Int(value.toInt())
                                    "LONG" -> columnValues[key] = Value.Long(value.toLong())
                                    "FLOAT" -> columnValues[key] = Value.Float(value.toFloat())
                                    "DOUBLE" -> columnValues[key] = Value.Double(value.toDouble())
                                    "DATETIME" -> columnValues[key] = Value.DateTime(SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(value))
                                    else -> throw(IllegalArgumentException("Type not supported"))
                                }
                            }
                        }
                    }
                    continue
                }

                // for each key value pair in this.field.parameters, check if the key is equal to the tag name
                for ((key, type) in this.field.parameters) {
                    if (tagname == key || fullname == key) {
                        columnValues[key] = convertType(directory, tag.tagType, type)
                    }
                }

            }
        }
        return listOf(MapStructDescriptor(UUID.randomUUID(), retrievable.id, this.field.parameters, columnValues.mapValues { it.value.value }))
    }
}