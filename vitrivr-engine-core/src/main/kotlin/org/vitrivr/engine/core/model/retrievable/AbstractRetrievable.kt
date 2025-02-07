package org.vitrivr.engine.core.model.retrievable

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import java.util.*

/**
 * An abstract implementation of a [Retrievable] implementing basic logic to manage [RetrievableAttribute]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 2.0.0
 */
abstract class AbstractRetrievable(
    override val id: UUID,
    override val type: String,
    override val content: List<ContentElement<*>>,
    override val descriptors: Set<Descriptor<*>>,
    override val attributes: Set<RetrievableAttribute>,
    override val relationships: Set<Relationship>,
    override val transient: Boolean
) : Retrievable {

    /**
     * Checks if this [Retrievable] has a [RetrievableAttribute] of the given type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to check for.
     * @return True, if [RetrievableAttribute]
     */
    override fun <T : RetrievableAttribute> hasAttribute(c: Class<T>): Boolean = this.attributes.any { c.isInstance(it) }

    /**
     * Returns all [RetrievableAttribute] of a certain type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to return.
     * @return [Collection] of [RetrievableAttribute]s.
     */
    override fun <T : RetrievableAttribute> filteredAttributes(c: Class<T>): Collection<T> = this.attributes.filterIsInstance(c)

    /**
     * Returns all [RetrievableAttribute] of a certain type.
     *
     * @return [Collection] of [RetrievableAttribute]s.
     */
    inline fun <reified T : RetrievableAttribute> filteredAttributes(): Collection<T> = filteredAttributes(T::class.java)

    /**
     * Returns the first [RetrievableAttribute] of a certain type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to return.
     * @return [RetrievableAttribute] or null.
     */
    override fun <T : RetrievableAttribute> filteredAttribute(c: Class<T>): T? = this.attributes.filterIsInstance(c).firstOrNull()

    /**
     * Returns the first [RetrievableAttribute] of a certain type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to return.
     * @return [RetrievableAttribute] or null.
     */
    inline fun <reified T : RetrievableAttribute> filteredAttribute(): T? = filteredAttribute(T::class.java)
}