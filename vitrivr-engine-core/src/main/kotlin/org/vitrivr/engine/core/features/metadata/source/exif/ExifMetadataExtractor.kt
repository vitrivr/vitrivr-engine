package org.vitrivr.engine.core.features.metadata.source.exif

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Directory
import com.drew.metadata.Metadata
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.MapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.FileSource
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ExifMetadataExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<ContentElement<*>, MapStructDescriptor>,
    persisting: Boolean = true
) : AbstractExtractor<ContentElement<*>, MapStructDescriptor>(input, field, persisting, bufferSize = 1) {
    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.filteredAttribute(SourceAttribute::class.java)?.source is FileSource /* && retrievable.type is MediaType.IMAGE */


    fun convertType(directory: Directory, tagType: Int, type: String): Any? {
        return when(type) {
            "STRING" -> directory.getString(tagType)
            "BOOLEAN" -> directory.getBoolean(tagType)
            "BYTE" -> directory.getObject(tagType) as Byte
            "SHORT" -> directory.getObject(tagType) as Short
            "INT" -> directory.getInt(tagType)
            "LONG" -> directory.getLong(tagType)
            "FLOAT" -> directory.getFloat(tagType)
            "DOUBLE" -> directory.getDouble(tagType)
            "DATETIME" -> SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(directory.getString(tagType))
            else -> throw(IllegalArgumentException("Type not supported"))
        }
    }

    override fun extract(retrievable: Retrievable): List<MapStructDescriptor> {
        var metadata = ImageMetadataReader.readMetadata((retrievable.filteredAttribute(SourceAttribute::class.java)?.source as FileSource).path.toFile())

        var columnValues = mutableMapOf<String, Any?>()

        for (directory in metadata.directories) {
            for (tag in directory.tags) {
                val tagname = tag.tagName.replace(Regex("[^a-zA-Z0-9]"), "")
                val fullname = "${directory.name.replace(Regex("[^a-zA-Z0-9]"), "")}_${tagname}"
                // for each key value pair in this.field.parameters, check if the key is equal to the tag name
                for ((key, type) in this.field.parameters) {
                    if (tagname == key || fullname == key) {
                        columnValues[key] = convertType(directory, tag.tagType, type)
                    }
                }

            }
        }
        return listOf(MapStructDescriptor(UUID.randomUUID(), retrievable.id, this.field.parameters, columnValues))
    }
}