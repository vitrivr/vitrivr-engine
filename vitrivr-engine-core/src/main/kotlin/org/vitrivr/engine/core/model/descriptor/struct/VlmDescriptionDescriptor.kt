package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * Stores a caption and its generation provenance for the original shot.
 *
 * @author Rahel Arnold
 */
class VlmDescriptionDescriptor(
    id: DescriptorId,
    retrievableId: RetrievableId?,
    values: Map<String, Value<*>?>,
    override val field: Schema.Field<*, VlmDescriptionDescriptor>? = null
) : StructDescriptor<VlmDescriptionDescriptor>(id, retrievableId, SCHEMA, values, field) {
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, VlmDescriptionDescriptor>?) =
        VlmDescriptionDescriptor(id, retrievableId, values, field)

    companion object {
        const val CAPTION = "caption"
        private val SCHEMA = listOf(
            Attribute(CAPTION, Type.Text),
            Attribute("model", Type.Text),
            Attribute("prompt_version", Type.Text),
            Attribute("generation_metadata", Type.Text)
        )
    }
}
