package org.vitrivr.engine.core.model.database.descriptor.scalar

import org.vitrivr.engine.core.model.database.descriptor.DescriptorId
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.DescriberId

/**
 * A [ScalarDescriptor] using a [Float] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class StringDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val transient: Boolean = false,
    override val describerId: DescriberId,
    override val value: String
) : ScalarDescriptor<String>
