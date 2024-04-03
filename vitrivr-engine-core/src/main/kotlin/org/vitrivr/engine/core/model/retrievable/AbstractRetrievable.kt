package org.vitrivr.engine.core.model.retrievable

import org.vitrivr.engine.core.model.retrievable.attributes.MergingRetrievableAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import java.util.*

/**
 * An abstract implementation of a [Retrievable] implementing basic logic to manage [RetrievableAttribute]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
abstract class AbstractRetrievable(override val id: UUID, override val type: String?, override val transient: Boolean) : Retrievable {

    constructor(retrievable: Retrievable) : this(retrievable.id, retrievable.type, retrievable.transient) {
        retrievable.attributes.forEach { this.addAttribute(it) }
    }

    /** A synchronized set of [RetrievableAttribute]s. */
    private val attributeSet = Collections.synchronizedSet(mutableSetOf<RetrievableAttribute>())

    /** [Collection] of [RetrievableAttribute]s held by this [AbstractRetrievable]. */
    override val attributes: Collection<RetrievableAttribute>
        get() = this.attributeSet

    /**
     * Adds a [RetrievableAttribute] to this [AbstractRetrievable].
     *
     * @param attribute The [RetrievableAttribute] to add.
     */
    override fun addAttribute(attribute: RetrievableAttribute) {
        if (this.attributeSet.contains(attribute)) {
            return
        }
        if (attribute is MergingRetrievableAttribute) {
            val other =
                this.attributeSet.find { it::class.java == attribute::class.java } as? MergingRetrievableAttribute
            if (other != null) {
                attributeSet.remove(other)
                attributeSet.add(other.merge(attribute))
            } else {
                attributeSet.add(attribute)
            }
        } else {
            this.attributeSet.add(attribute)
        }
    }

    /**
     * Removes all [RetrievableAttribute]s  of a certain type from this [AbstractRetrievable].
     *
     * @param c The [Class] of the [RetrievableAttribute] to remove.
     */
    override fun <T : RetrievableAttribute> removeAttributes(c: Class<T>) {
        this.attributeSet.removeIf { c.isInstance(it) }
    }

    /**
     * Checks if this [Retrievable] has a [RetrievableAttribute] of the given type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to check for.
     * @return True, if [RetrievableAttribute]
     */
    override fun <T : RetrievableAttribute> hasAttribute(c: Class<T>): Boolean = this.attributeSet.any { c.isInstance(it) }

    /**
     * Returns all [RetrievableAttribute] of a certain type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to return.
     * @return [Collection] of [RetrievableAttribute]s.
     */
    override fun <T : RetrievableAttribute> filteredAttributes(c: Class<T>): Collection<T> = this.attributeSet.filterIsInstance(c)

    inline fun <reified T : RetrievableAttribute> filteredAttributes(): Collection<T> = filteredAttributes(T::class.java)

    /**
     * Returns the first [RetrievableAttribute] of a certain type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to return.
     * @return [RetrievableAttribute] or null.
     */
    override fun <T : RetrievableAttribute> filteredAttribute(c: Class<T>): T? = this.attributeSet.filterIsInstance(c).firstOrNull()

    inline fun <reified T : RetrievableAttribute> filteredAttribute(): T? = filteredAttribute(T::class.java)
}