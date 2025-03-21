package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.DescriptorId
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
class ShotBoundaryDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?, //retrievable Id must come first, due to reflection
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, ShotBoundaryDescriptor>? = null
) : StructDescriptor<ShotBoundaryDescriptor>(id, retrievableId, SCHEMA, values, field) {

    companion object {
        /** The field schema associated with a [ShotBoundaryDescriptor]. */
        private val SCHEMA = listOf(
            Attribute("starts", Type.String),
            Attribute("ends", Type.String),
        )

        /** The prototype [ShotBoundaryDescriptor]. */
        val PROTOTYPE = ShotBoundaryDescriptor(UUID.randomUUID(), UUID.randomUUID(), mapOf("starts" to Value.String("[0,1000000000]"), "ends" to Value.String("[1000000000,2000000000]")))
    }

    /** The start timestamp in nanoseconds. */
    val start: Value.Long by this.values

    /** The end timestamp in nanoseconds. */
    val end: Value.Long by this.values

    /**
     * Returns a copy of this [ShotBoundaryDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [ShotBoundaryDescriptor].
     * @param retrievableId [RetrievableId] of the new [ShotBoundaryDescriptor].
     * @param field [Schema.Field] the new [ShotBoundaryDescriptor] belongs to.
     * @return Copy of this [ShotBoundaryDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, ShotBoundaryDescriptor>?) = ShotBoundaryDescriptor(id, retrievableId, HashMap(this.values), field)
}