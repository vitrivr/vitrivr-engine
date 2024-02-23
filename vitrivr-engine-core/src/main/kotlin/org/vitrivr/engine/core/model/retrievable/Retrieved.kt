package org.vitrivr.engine.core.model.retrievable

import org.vitrivr.engine.core.model.retrievable.attributes.MergingRetrievableAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import java.util.*

/**
 * A [Retrievable] that has been generated as part of the retrieval process.
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.1.0
 */
data class Retrieved(override val id: UUID, override val type: String?, override val transient: Boolean) : Retrievable {

    private val attributeSet = mutableSetOf<RetrievableAttribute>()
    override val attributes: Collection<RetrievableAttribute>
        get() = this.attributeSet

    override fun <T : RetrievableAttribute> filteredAttributes(c: Class<T>): Collection<T> =
        this.attributeSet.filterIsInstance(c)

    override fun <T : RetrievableAttribute> filteredAttribute(c: Class<T>): T? = attributeSet.firstOrNull { c.isInstance(it) } as? T

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

    override fun <T : RetrievableAttribute> removeAttributes(c: Class<T>) {
        this.attributeSet.removeIf { c.isInstance(it) }
    }

}