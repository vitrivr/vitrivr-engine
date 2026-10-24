package org.vitrivr.engine.core.features.metadata.recording

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.RecordingMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.file.FileSource
import java.util.UUID
import kotlin.io.path.absolutePathString

/** Extracts inexpensive recording provenance for source videos and decoded results. */
class RecordingMetadataExtractor(
    input: Operator<out Retrievable>, analyser: RecordingMetadata,
    field: Schema.Field<ContentElement<*>, RecordingMetadataDescriptor>?, name: String, private val context: Context
) : AbstractExtractor<ContentElement<*>, RecordingMetadataDescriptor>(input, analyser, field, name) {
    constructor(input: Operator<out Retrievable>, analyser: RecordingMetadata,
                field: Schema.Field<ContentElement<*>, RecordingMetadataDescriptor>, context: Context) :
        this(input, analyser, field, field.fieldName, context)
    constructor(input: Operator<out Retrievable>, analyser: RecordingMetadata, name: String, context: Context) :
        this(input, analyser, null, name, context)

    private fun names(key: String) = (field?.parameters?.get(key) ?: context[name, key])
        ?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    private val persons = names("persons")
    private val locations = names("locations")

    init {
        require(persons.intersect(locations).isEmpty()) { "A recording source cannot be both person and room." }
    }

    override fun matches(retrievable: Retrievable) =
        retrievable.filteredAttribute(SourceAttribute::class.java)?.source is FileSource

    override fun extract(retrievable: Retrievable): List<RecordingMetadataDescriptor> {
        val source = retrievable.filteredAttribute(SourceAttribute::class.java)?.source as FileSource
        val recording = RecordingPath.parse(source.path.absolutePathString(), persons, locations)
            ?: return emptyList()
        return listOf(RecordingMetadataDescriptor(UUID.randomUUID(), retrievable.id, mapOf(
            "day" to Value.String(recording.day), "source" to Value.String(recording.source),
            "source_type" to Value.String(recording.sourceType)
        ), field))
    }
}
