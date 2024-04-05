package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [StructDescriptor] used to store temporal metadata.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class TemporalMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId, //retrievable Id must come first, due to reflection
    val startNs: Value.Long,
    val endNs: Value.Long,
    override val transient: Boolean = false
) : StructDescriptor {

    companion object {
        /** The field schema associated with a [TemporalMetadataDescriptor]. */
        private val SCHEMA = listOf(
            FieldSchema("start", Type.LONG),
            FieldSchema("end", Type.LONG),
        )

        /** The prototype [TemporalMetadataDescriptor]. */
        val PROTOTYPE = TemporalMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Long(0L), Value.Long(0L))
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
    override fun values(): List<Pair<String, Any?>> = listOf(
        "start" to this.startNs,
        "end" to this.endNs
    )
}