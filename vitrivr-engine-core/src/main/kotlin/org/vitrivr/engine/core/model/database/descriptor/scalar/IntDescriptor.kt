package org.vitrivr.engine.core.model.database.descriptor.scalar

import org.vitrivr.engine.core.model.database.descriptor.DescriptorId
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId

/**
 * A [ScalarDescriptor] using an [Int] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class IntDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val transient: Boolean,
    override val value: Int,
): ScalarDescriptor<Int>