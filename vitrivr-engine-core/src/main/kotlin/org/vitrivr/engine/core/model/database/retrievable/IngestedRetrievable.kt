package org.vitrivr.engine.core.model.database.retrievable

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.DerivedContent
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.operators.derive.ContentDerivers
import org.vitrivr.engine.core.operators.derive.DerivateName
import java.util.*
import kotlin.collections.HashSet

/**
 * A [Retrievable] used in the data ingest pipeline.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface IngestedRetrievable: Retrievable {

    /** List of [Content] elements that make-up this [IngestedRetrievable]. */
    val content: List<Content>

    /** List of [Descriptor] elements that have been extracted for this [IngestedRetrievable]. */
    val descriptors: List<Descriptor>

    /**
     * Adds a [Content] to this [IngestedRetrievable].
     *
     * @param content The [Content] element to add.
     */
    fun addContent(content: Content)

    /**
     * Adds a [Descriptor] to this [IngestedRetrievable].
     *
     * @param descriptor The [Descriptor] element to add.
     */
    fun addDescriptor(descriptor: Descriptor)

    fun getDerivedContent(name: DerivateName) : DerivedContent?

    /**
     * A [Default] implementation of an [IngestedRetrievable].
     */
    class Default(
        override val id: UUID = UUID.randomUUID(),
        override val transient: Boolean,
        partOf: Set<Retrievable> = emptySet(),
        parts: Set<Retrievable> = emptySet(),
        content: List<Content> = emptyList(),
        descriptors: List<Descriptor> = emptyList()
    ): IngestedRetrievable {

        /** Set of [Retrievable]s this [IngestedRetrievable] is part of. */
        override val partOf: Set<Retrievable> = Collections.synchronizedSet(HashSet())

        /** Set of [Retrievable]s that are part of this [IngestedRetrievable]. */
        override val parts: Set<Retrievable> = Collections.synchronizedSet(HashSet())

        /** List of [Content] elements associated with this [IngestedRetrievable.Default]. */
        override val content: List<Content> = Collections.synchronizedList(LinkedList())

        /** List of [Descriptor]s with this [IngestedRetrievable.Default]. */
        override val descriptors: List<Descriptor> = Collections.synchronizedList(LinkedList())

        init {
            (this.partOf as MutableSet).addAll(partOf)
            (this.parts as MutableSet).addAll(parts)
            (this.content as MutableList).addAll(content)
            (this.descriptors as MutableList).addAll(descriptors)
        }

        /**
         * Adds a [Content] to this [IngestedRetrievable.Default].
         *
         * @param content The [Content] element to add.
         */
        override fun addContent(content: Content) {
            (this.content as MutableList).add(content)
        }

        /**
         * Adds a [Descriptor] to this [IngestedRetrievable.Default].
         *
         * @param descriptor The [Descriptor] element to add.
         */
        override fun addDescriptor(descriptor: Descriptor) {
            (this.descriptors as MutableList).add(descriptor)
        }

        private val derivedContentCache: LoadingCache<DerivateName, DerivedContent> = Caffeine.newBuilder().build { name ->
            ContentDerivers[name]?.derive(this)
        }
        override fun getDerivedContent(name: DerivateName): DerivedContent? = derivedContentCache[name]
        override fun toString(): String {
            return "IngestedRetrievable.Default(id=$id, transient=$transient, partOf=$partOf, parts=$parts, content=$content, descriptors=$descriptors)"
        }
    }
}