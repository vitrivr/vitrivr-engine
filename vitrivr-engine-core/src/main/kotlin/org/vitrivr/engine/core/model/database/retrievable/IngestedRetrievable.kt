package org.vitrivr.engine.core.model.database.retrievable

import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import java.util.*

/**
 * A [Retrievable] used in the data ingest pipeline.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface IngestedRetrievable: Retrievable {

    /** List of [Content] elements that make-up this [IngestedRetrievable]. */
    val content: MutableList<Content>

    /** List of [Descriptor] elements that have been extracted for this [IngestedRetrievable]. */
    val descriptors: MutableList<Descriptor>

    /**
     * A [Default] implementation of an [IngestedRetrievable].
     */
    @JvmRecord
    data class Default(
        override val id: UUID = UUID.randomUUID(),
        override val transient: Boolean,
        override val partOf: MutableSet<Retrievable>,
        override val parts: MutableSet<Retrievable>,
        override val content: MutableList<Content>,
        override val descriptors: MutableList<Descriptor>
    ): IngestedRetrievable
}