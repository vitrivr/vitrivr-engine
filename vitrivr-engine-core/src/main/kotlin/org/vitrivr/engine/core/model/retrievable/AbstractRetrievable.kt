package org.vitrivr.engine.core.model.retrievable

import org.vitrivr.engine.core.model.retrievable.attributes.MergingRetrievableAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import java.util.*

abstract class AbstractRetrievable(override val id: UUID, override val type: String?, override val transient: Boolean) : Retrievable {

    constructor(retrievable: Retrievable) : this(retrievable.id, retrievable.type, retrievable.transient) {
        retrievable.attributes.forEach { this.addAttribute(it) }
    }

    private val attributeSet = mutableSetOf<RetrievableAttribute>()
    override val attributes: Collection<RetrievableAttribute>
        get() = this.attributeSet

    override fun <T : RetrievableAttribute> filteredAttributes(c: Class<T>): Collection<T> = this.attributeSet.filterIsInstance(c)

    inline fun <reified T : RetrievableAttribute> filteredAttributes(): Collection<T> = filteredAttributes(T::class.java)

    @Suppress("UNCHECKED_CAST")
    override fun <T : RetrievableAttribute> filteredAttribute(c: Class<T>): T? = attributeSet.firstOrNull { c.isInstance(it) } as? T

    inline fun <reified T : RetrievableAttribute> filteredAttribute(): T? = filteredAttribute(T::class.java)

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