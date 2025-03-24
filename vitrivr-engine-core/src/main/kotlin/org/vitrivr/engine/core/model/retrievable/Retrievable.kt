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
 * @version 2.0.0
 */
interface Retrievable : Persistable {
    /** The [RetrievableId] held by this [Retrievable]. */
    val id: RetrievableId

    /** The type of this [Retrievable]. This is basically a string that can help to keep apart different types of [Retrievable]. */
    val type: String

    /** The [ContentElement]s held by this [Retrievable]. */
    val content: List<ContentElement<*>>

    /** The [Descriptor]s held by this [Retrievable]- */
    val descriptors: Set<Descriptor<*>>

    /** The [RetrievableAttribute]s held by this [Retrievable]. */
    val attributes: Set<RetrievableAttribute>

    /** The [Relationship]s held by [Retrievable]. */
    val relationships: Set<Relationship>

    /**
     * Checks if this [Retrievable] has a [RetrievableAttribute] of the given type.
     *
     * @param c The [Class] of the [RetrievableAttribute] to check for.
     * @return True, if [RetrievableAttribute]
     */
    fun <T : RetrievableAttribute> hasAttribute(c: Class<T>): Boolean

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
     * Finds all [ContentElement]s held by this [Retrievable] that satisfy the given [Predicate].
     *
     * @param predicate The [Predicate] to test the [ContentElement]s against.
     * @return List of matching [ContentElement]s
     */
    fun findContent(predicate: Predicate<ContentElement<*>>): List<ContentElement<*>> = this.content.filter { predicate.test(it) }

    /**
     * Finds all [Descriptor]s held by this [Retrievable] that satisfy the given [Predicate].
     *
     * @param predicate The [Predicate] to test the [Descriptor]s against.
     * @return List of matching [Descriptor]s
     */
    fun findDescriptor(predicate: Predicate<Descriptor<*>>): List<Descriptor<*>> = this.descriptors.filter { predicate.test(it) }

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
    fun copy(id: RetrievableId? = null, type: String? = null, content: List<ContentElement<*>>? = null, descriptors: Collection<Descriptor<*>>? = null, attributes: Collection<RetrievableAttribute>? = null, relationships: Collection<Relationship>? = null, transient: Boolean? = null): Retrievable
}