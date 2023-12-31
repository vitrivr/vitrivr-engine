package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [StructDescriptor] used to store metadata about a file.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class FileSourceMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val path: String,
    val size: Long,
    override val transient: Boolean = false
) : StructDescriptor {

    companion object {
        private val SCHEMA = listOf(
            FieldSchema("path", FieldType.STRING),
            FieldSchema("size", FieldType.LONG),
        )
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