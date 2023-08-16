package org.vitrivr.engine.core.data.descriptor

import org.vitrivr.engine.core.describe.DescriberId
import java.util.*

data class LabelDescriptor(
    override val id: UUID = UUID.randomUUID(),
    override val transient: Boolean = false,
    override val describerId: DescriberId,
    val label: String
) : Descriptor
