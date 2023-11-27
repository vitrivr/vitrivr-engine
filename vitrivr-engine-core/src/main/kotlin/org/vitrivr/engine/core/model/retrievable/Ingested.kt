package org.vitrivr.engine.core.model.retrievable

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithDescriptor
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithRelationship
import java.util.*

/**
 * A [Ingested] used in the data ingest pipeline.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
open class Ingested(
    override val id: RetrievableId,
    override val type: String?,
    override val transient: Boolean,
    content: List<ContentElement<*>> = emptyList(),
    descriptors: List<Descriptor> = emptyList(),
    relationships: Set<Relationship> = emptySet(),
) : Retrievable, RetrievableWithDescriptor, RetrievableWithContent, RetrievableWithRelationship {
    override val content: List<ContentElement<*>> = LinkedList(content)                     /* Make shallow copy. */
    override val descriptors: List<Descriptor> = LinkedList(descriptors)                    /* Make shallow copy. */
    override val relationships: MutableSet<Relationship> = HashSet(relationships)  /* Make shallow copy. */
}