package org.vitrivr.engine.core.model.database.descriptor

import org.vitrivr.engine.core.model.database.Persistable
import org.vitrivr.engine.core.model.database.retrievable.Retrievable
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.Describer
import org.vitrivr.engine.core.operators.DescriberId
import java.util.UUID


/** A typealias to identify the [UUID] identifying a [Descriptor]. */
typealias DescriptorId = UUID

/**
 * A [Persistable] [Descriptor] that can be used to describe a [Retrievable].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Descriptor : Persistable {
    /** The [DescriptorId] held by this [Descriptor]. */
    override val id: DescriptorId

    /** The [RetrievableId] of the [Retrievable] that is being described by this [Descriptor]. */
    val retrievableId: RetrievableId

    /** ID of the [Describer] that produced this [Descriptor]. */
    val describerId: DescriberId
}