package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [StructDescriptor] used to store temporal metadata about a [Retrievable].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class TemporalMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val startNs: Long,
    val endNs: Long,
    override val transient: Boolean = false
) : StructDescriptor {

    companion object {
        private val SCHEMA = listOf(
            FieldSchema("start", FieldType.LONG),
            FieldSchema("end", FieldType.LONG),
        )
    }

    /**
     * Returns the [FieldSchema] [List ]of this [TemporalMetadataDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA

    /**
     * Returns the fields and its values of this [TemporalMetadataDescriptor] as a [Map].
     *
     * @return A [Map] of this [TemporalMetadataDescriptor]'s fields (without the IDs).
     */
    override fun values(): Map<String, Any?> = mapOf(
        "start" to this.startNs,
        "end" to this.endNs
    )
}