package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [ScalarDescriptor] using a [String] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class StringDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val value: String,
    override val transient: Boolean = false,
) : ScalarDescriptor<String>
