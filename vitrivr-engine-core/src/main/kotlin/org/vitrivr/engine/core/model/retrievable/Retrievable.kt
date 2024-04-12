package org.vitrivr.engine.core.model.retrievable

import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import org.vitrivr.engine.core.model.retrievable.relationship.Relationship
import java.util.*
import java.util.function.Predicate

/** A typealias to identify the [UUID] identifying a [Retrievable]. */
typealias RetrievableId = UUID

/**
 * A [Persistable] and [Retrievable] unit of information stored by vitrivr.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.2.0
 */
interface Retrievable : Persistable {
    /** The [RetrievableId] held by this [Retrievable]. */
    override val id: RetrievableId

    /** The type of this [Retrievable]. This is basically a string that can help to keep apart different types of [Retrievable]. */
    val type: String?

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
     */
    fun findRelationship(predicate: Predicate<Relationship>): List<Relationship> = this.relationships.filter { predicate.test(it) }
}