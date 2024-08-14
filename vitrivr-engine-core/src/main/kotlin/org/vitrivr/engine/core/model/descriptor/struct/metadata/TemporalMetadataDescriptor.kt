package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.struct.MapStructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [StructDescriptor] used to store temporal metadata.
 *
 * @author Ralph Gasser
 * @version 2.0.0
 */
class TemporalMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?, //retrievable Id must come first, due to reflection
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, TemporalMetadataDescriptor>? = null
) : MapStructDescriptor(id, retrievableId, SCHEMA, values, field) {

    companion object {
        /** The field schema associated with a [TemporalMetadataDescriptor]. */
        private val SCHEMA = listOf(
            Attribute("start", Type.Long),
            Attribute("end", Type.Long),
        )

        /** The prototype [TemporalMetadataDescriptor]. */
        val PROTOTYPE = TemporalMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), mapOf("start" to Value.Long(0L), "end" to Value.Long(0L)))
    }

    /** The start timestamp in nanoseconds. */
    val start: Value.Long by this.values

    /** The end timestamp in nanoseconds. */
    val end: Value.Long by this.values

    /**
     * Returns a copy of this [TemporalMetadataDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [TemporalMetadataDescriptor].
     * @param retrievableId [RetrievableId] of the new [TemporalMetadataDescriptor].
     * @return Copy of this [TemporalMetadataDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?) = TemporalMetadataDescriptor(id, retrievableId, HashMap(this.values), this.field)
}