package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.UUID

/** Recording provenance, independent of the people recognised in an image. */
class RecordingMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    values: Map<String, Value<*>?>,
    override val field: Schema.Field<*, RecordingMetadataDescriptor>? = null
) : StructDescriptor<RecordingMetadataDescriptor>(id, retrievableId, SCHEMA, values, field) {
    val day: Value.String by this.values
    val source: Value.String by this.values
    val sourceType: Value.String get() = this.values.getValue("source_type") as Value.String

    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, RecordingMetadataDescriptor>?) =
        RecordingMetadataDescriptor(id, retrievableId, HashMap(this.values), field)

    companion object {
        private val SCHEMA = listOf(
            Attribute("day", Type.String), Attribute("source", Type.String), Attribute("source_type", Type.String)
        )
        val PROTOTYPE = RecordingMetadataDescriptor(UUID.randomUUID(), null, mapOf(
            "day" to Value.String(""), "source" to Value.String(""), "source_type" to Value.String("unknown")
        ))
    }
}
