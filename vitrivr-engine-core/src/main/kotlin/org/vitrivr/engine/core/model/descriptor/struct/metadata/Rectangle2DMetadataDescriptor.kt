package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [StructDescriptor] used to store temporal metadata about a [Retrievable].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class Rectangle2DMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val leftX: Int,
    val leftY: Int,
    val width: Int,
    val height: Int,
    override val transient: Boolean = false
) : StructDescriptor