package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [StructDescriptor] used to store metadata about a file.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class FileMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val path: String,
    val size: Long,
    override val transient: Boolean = false
) : StructDescriptor {
    override fun asMap(): Map<String, Any?> = mapOf(
        "path" to this.path,
        "size" to this.size
    )
}