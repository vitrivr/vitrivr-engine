package org.vitrivr.engine.core.model.database.descriptor.struct

import org.vitrivr.engine.core.model.database.descriptor.DescriptorId
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId

/**
 * A [StructDescriptor] used to store a string label and a confidence for that label.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class LabelDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val transient: Boolean = false,
    val label: String,
    val confidence: Float
) : StructDescriptor
