package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [StructDescriptor] used to store a string label and a confidence for that label.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class LabelDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val label: String,
    val confidence: Float,
    override val transient: Boolean = false
) : StructDescriptor