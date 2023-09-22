package org.vitrivr.engine.core.model.database.descriptor.scalar

import org.vitrivr.engine.core.model.database.descriptor.DescriptorId
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId

/**
 * A [ScalarDescriptor] using a [Boolean] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class BooleanDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val transient: Boolean,
    override val value: Boolean,
): ScalarDescriptor<Boolean>