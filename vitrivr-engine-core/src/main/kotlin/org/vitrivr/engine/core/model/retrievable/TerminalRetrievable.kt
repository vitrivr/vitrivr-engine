package org.vitrivr.engine.core.model.retrievable

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import java.util.*

/**
 * This is a singleton [Retrievable] that is used to indicate the end of a stream of [Retrievable]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data object TerminalRetrievable: Retrievable {
    override val id: RetrievableId = UUID.fromString("0000000-0000-0000-0000-000000000000")
    override val type: String = "TERMINAL"
    override val content: List<ContentElement<*>> = emptyList()
    override val descriptors: Set<Descriptor<*>> = emptySet()
    override val attributes: Set<RetrievableAttribute> = emptySet()
    override val relationships: Set<Relationship> = emptySet()
    override val transient: Boolean = true
    override fun <T : RetrievableAttribute> hasAttribute(c: Class<T>): Boolean  = false
    override fun <T : RetrievableAttribute> filteredAttributes(c: Class<T>): Collection<T> = emptyList()
    override fun <T : RetrievableAttribute> filteredAttribute(c: Class<T>): T? = null

    override fun copy(
        id: RetrievableId?,
        type: String?,
        content: List<ContentElement<*>>?,
        descriptors: Collection<Descriptor<*>>?,
        attributes: Collection<RetrievableAttribute>?,
        relationships: Collection<Relationship>?,
        transient: Boolean?
    ) = TerminalRetrievable
}