package org.vitrivr.engine.core.model.database.descriptor.scalar

import org.vitrivr.engine.core.model.database.descriptor.DescriptorId
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.DescriberId

/**
 * A [ScalarDescriptor] using a [Long] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
data class LongDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val describerId: DescriberId,
    override val transient: Boolean,
    override val value: Long,
): ScalarDescriptor<Long>