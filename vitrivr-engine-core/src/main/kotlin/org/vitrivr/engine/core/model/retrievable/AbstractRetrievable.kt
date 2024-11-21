package org.vitrivr.engine.core.model.retrievable

import org.vitrivr.engine.core.database.blackhole.LOGGER
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.attributes.MergingRetrievableAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import java.util.*

/**
 * An abstract implementation of a [Retrievable] implementing basic logic to manage [RetrievableAttribute]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 2.0.0
 */
abstract class AbstractRetrievable(override val id: UUID, override val type: String?, override val transient: Boolean) : Retrievable {

    /** A set of [ContentElement]s held by this [AbstractRetrievable]. */
    private val contentList = mutableListOf<ContentElement<*>>()

    /** A set of [Descriptor]s held by this [AbstractRetrievable]. */
    private val descriptorSet = mutableSetOf<Descriptor<*>>()

    /** A set of [RetrievableAttribute]s held by this [AbstractRetrievable]. */
    private val attributeSet = mutableSetOf<RetrievableAttribute>()

    /** A set of [Relationship]s. */
    private val relationshipSet = mutableSetOf<Relationship>()

    /** [Collection] of [ContentElement]s held by this [AbstractRetrievable]. */
    override val content: List<ContentElement<*>>
        get() = Collections.unmodifiableList(this.contentList)

    /** [Collection] of [Descriptor]s held by this [AbstractRetrievable]. */
    override val descriptors: Collection<Descriptor<*>>
        get() = Collections.unmodifiableSet(this.descriptorSet)

    /** [Collection] of [RetrievableAttribute]s held by this [AbstractRetrievable]. */
    override val attributes: Collection<RetrievableAttribute>
        get() = Collections.unmodifiableSet(this.attributeSet)

    /** [Collection] of [Relationship]s held by this [AbstractRetrievable]. */
    override val relationships: Collection<Relationship>
        get() = Collections.unmodifiableSet(this.relationshipSet)

    /**
     * Adds a [RetrievableAttribute] to this [AbstractRetrievable].
     *
     * @param attribute The [RetrievableAttribute] to add.
     */
    @Synchronized
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
    @Synchronized
    override fun <T : RetrievableAttribute> removeAttributes(c: Class<T>) {
        this.attributeSet.removeIf { c.isInstance(it) }
    }

    /**
     * Checks if this [Retrievable] has a [RetrievableAttribute] of the given type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to check for.
     * @return True, if [RetrievableAttribute]
     */
    @Synchronized
    override fun <T : RetrievableAttribute> hasAttribute(c: Class<T>): Boolean = this.attributeSet.any { c.isInstance(it) }

    /**
     * Returns all [RetrievableAttribute] of a certain type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to return.
     * @return [Collection] of [RetrievableAttribute]s.
     */
    @Synchronized
    override fun <T : RetrievableAttribute> filteredAttributes(c: Class<T>): Collection<T> = this.attributeSet.filterIsInstance(c)

    inline fun <reified T : RetrievableAttribute> filteredAttributes(): Collection<T> = filteredAttributes(T::class.java)

    /**
     * Returns the first [RetrievableAttribute] of a certain type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to return.
     * @return [RetrievableAttribute] or null.
     */
    @Synchronized
    override fun <T : RetrievableAttribute> filteredAttribute(c: Class<T>): T? = this.attributeSet.filterIsInstance(c).firstOrNull()
    inline fun <reified T : RetrievableAttribute> filteredAttribute(): T? = filteredAttribute(T::class.java)

    /**
     * Adds a [ContentElement] to this [AbstractRetrievable].
     *
     * @param content [ContentElement] to add.
     * @return True on success, false otherwise.
     */
    @Synchronized
    override fun addContent(content: ContentElement<*>): Boolean = this.contentList.add(content)

    /**
     * Removes a [ContentElement] from this [AbstractRetrievable].
     *
     * @param content [ContentElement] to remove.
     * @return True on success, false otherwise.
     */
    @Synchronized
    override fun removeContent(content: ContentElement<*>): Boolean = this.contentList.remove(content)

    /**
     * Removes all [ContentElement] associated with this [AbstractRetrievable].
     */
    @Synchronized
    override fun clearContent() = this.contentList.clear()

    /**
     * Adds a [Descriptor] to this [AbstractRetrievable].
     *
     * @param descriptor [Descriptor] to add.
     * @return True on success, false otherwise.
     */
    @Synchronized
    override fun addDescriptor(descriptor: Descriptor<*>): Boolean = this.descriptorSet.add(descriptor)

    /**
     * Removes a [Descriptor] from this [AbstractRetrievable].
     *
     * @param descriptor [Descriptor] to remove.
     * @return True on success, false otherwise.
     */
    @Synchronized
    override fun removeDescriptor(descriptor: Descriptor<*>): Boolean = this.descriptorSet.remove(descriptor)

    /**
     * Adds a [Relationship] to this [AbstractRetrievable].
     *
     * @param relationship [Relationship] to add.
     * @return True on success, false otherwise.
     */
    @Synchronized
    override fun addRelationship(relationship: Relationship): Boolean {
        check(relationship.subjectId == this.id || relationship.objectId == this.id) {
            LOGGER.error { "Relationship is not related to current retrievable and therefore cannot be added." }
        }
        if (this.relationshipSet.add(relationship)) {
            if (relationship.subjectId == this.id && relationship is Relationship.WithObject) {
                relationship.`object`.addRelationship(relationship)
            } else if (relationship.objectId == this.id && relationship is Relationship.WithSubject) {
                relationship.subject.addRelationship(relationship)
            }
            return true
        } else {
            return false
        }
    }

    /**
     * Removes a [Relationship] from this [AbstractRetrievable].
     *
     * @param relationship [Relationship] to remove.
     * @return True on success, false otherwise.
     */
    @Synchronized
    override fun removeRelationship(relationship: Relationship): Boolean = this.relationshipSet.remove(relationship)


}