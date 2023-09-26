package org.vitrivr.engine.core.model.database.descriptor.scalar

import org.vitrivr.engine.core.model.database.descriptor.DescriptorId
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId

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
