package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [StructDescriptor] used to store metadata about a file.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class FileSourceMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val path: Value.String,
    val size: Value.Long,
    override val field: Schema.Field<*, FileSourceMetadataDescriptor>? = null
) : StructDescriptor {

    companion object {
        /** The field schema associated with a [FileSourceMetadataDescriptor]. */
        private val SCHEMA = listOf(
            FieldSchema("path", Type.STRING),
            FieldSchema("size", Type.LONG),
        )

        /** The prototype [FileSourceMetadataDescriptor]. */
        val PROTOTYPE = FileSourceMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.String(""), Value.Long(0L))
    }

    /**
     * Returns the [FieldSchema] [List ]of this [StructDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA

    /**
     * Returns the fields and its values of this [FileSourceMetadataDescriptor] as a [Map].
     *
     * @return A [Map] of this [FileSourceMetadataDescriptor]'s fields (without the IDs).
     */
    override fun values(): List<Pair<String, Any?>> = listOf(
        "path" to this.path,
        "size" to this.size
    )
}