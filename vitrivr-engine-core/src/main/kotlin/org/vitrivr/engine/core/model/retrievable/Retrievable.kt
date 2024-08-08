package org.vitrivr.engine.core.model.retrievable

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import org.vitrivr.engine.core.model.serializer.UUIDSerializer
import java.util.*
import java.util.function.Predicate

/** A typealias to identify the [UUID] identifying a [Retrievable]. */
typealias RetrievableId = @Serializable(UUIDSerializer::class) UUID

/**
 * A [Persistable] and [Retrievable] unit of information stored by vitrivr.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.3.0
 */
interface Retrievable : Persistable {
    /** The [RetrievableId] held by this [Retrievable]. */
    val id: RetrievableId

    /** The type of this [Retrievable]. This is basically a string that can help to keep apart different types of [Retrievable]. */
    val type: String?

    /** The [ContentElement]s held by this [Retrievable]. */
    val content: List<ContentElement<*>>

    /** The [Descriptor]s held by this [Retrievable]- */
    val descriptors: Collection<Descriptor>

    /** The [RetrievableAttribute]s held by this [Retrievable]. */
    val attributes: Collection<RetrievableAttribute>

    /** The [Relationship]s held by [Retrievable]. */
    val relationships: Collection<Relationship>

    /**
     * Checks if this [Retrievable] has a [RetrievableAttribute] of the given type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to check for.
     * @return True, if [RetrievableAttribute]
     */
    fun <T : RetrievableAttribute> hasAttribute(c: Class<T>): Boolean

    /**
     * Adds a [RetrievableAttribute] to this [Retrievable].
     *
     * @param attribute The [RetrievableAttribute] to add.
     */
    fun addAttribute(attribute: RetrievableAttribute)

    /**
     * Removes all [RetrievableAttribute]s  of a certain type from this [Retrievable].
     *
     * @param c The [Class] of the [RetrievableAttribute] to remove.
     */
    fun <T : RetrievableAttribute> removeAttributes(c: Class<T>)

    /**
     * Returns all [RetrievableAttribute] of a certain type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to return.
     * @return [Collection] of [RetrievableAttribute]s.
     */
    fun <T : RetrievableAttribute> filteredAttributes(c: Class<T>): Collection<T>

    /**
     * Returns the first [RetrievableAttribute] of a certain type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to return.
     * @return [RetrievableAttribute] or null.
     */
    fun <T : RetrievableAttribute> filteredAttribute(c: Class<T>): T?

    /**
     * Adds a [ContentElement] to this [Retrievable].
     *
     * @param content The [ContentElement] to add.
     * @return True on success, false otherwise.
     */
    fun addContent(content: ContentElement<*>): Boolean

    /**
     * Removes a [ContentElement] from this [Retrievable].
     *
     * @param content The [ContentElement] to remove.
     * @return True on success, false otherwise.
     */
    fun removeContent(content: ContentElement<*>): Boolean

    /**
     * Removes all [ContentElement]s associated with this [Retrievable].
     */
    fun clearContent()

    /**
     * Finds all [ContentElement]s held by this [Retrievable] that satisfy the given [Predicate].
     *
     * @param predicate The [Predicate] to test the [ContentElement]s against.
     * @return List of matching [ContentElement]s
     */
    fun findContent(predicate: Predicate<ContentElement<*>>): List<ContentElement<*>> = this.content.filter { predicate.test(it) }

    /**
     * Adds a [Descriptor] to this [Retrievable].
     *
     * @param descriptor The [Descriptor] to add.
     * @return True on success, false otherwise.
     */
    fun addDescriptor(descriptor: Descriptor): Boolean

    /**
     * Removes a [Descriptor] from this [Retrievable].
     *
     * @param descriptor The [Descriptor] to remove.
     * @return True on success, false otherwise.
     */
    fun removeDescriptor(descriptor: Descriptor): Boolean

    /**
     * Finds all [Descriptor]s held by this [Retrievable] that satisfy the given [Predicate].
     *
     * @param predicate The [Predicate] to test the [Descriptor]s against.
     * @return List of matching [Descriptor]s
     */
    fun findDescriptor(predicate: Predicate<Descriptor>): List<Descriptor> = this.descriptors.filter { predicate.test(it) }

    /**
     * Adds a [Relationship] to this [Retrievable].
     *
     * @param relationship The [Relationship] to add.
     * @return True on success, false otherwise.
     */
    fun addRelationship(relationship: Relationship): Boolean

    /**
     * Removes all [Relationship]s from this [Retrievable].
     *
     * @param relationship The [Relationship] to remove.
     * @return True on success, false otherwise.
     */
    fun removeRelationship(relationship: Relationship): Boolean

    /**
     * Finds all [Relationship]s held by this [Retrievable] that satisfy the given [Predicate]
     *
     *  @param predicate The [Predicate] to test the [Relationship]s against.
     * @return List of matching [Relationship]s
     */
    fun findRelationship(predicate: Predicate<Relationship>): List<Relationship> = this.relationships.filter { predicate.test(it) }

    /**
     * Creates a copy of this [Retrievable]
     */
    fun copy(): Retrievable
}