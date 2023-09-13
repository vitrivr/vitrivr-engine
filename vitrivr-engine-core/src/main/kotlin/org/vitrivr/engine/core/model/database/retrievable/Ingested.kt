package org.vitrivr.engine.core.model.database.retrievable

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.DerivedContent
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.operators.derive.ContentDerivers
import org.vitrivr.engine.core.operators.derive.DerivateName
import java.util.*

/**
 * A default [Retrievable] used in the data ingest pipeline.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Ingested : Retrievable {

    /**
     *
     */
    class Default(
        override val id: UUID = UUID.randomUUID(),
        override val type: String,
        override val transient: Boolean,
        partOf: Set<Retrievable> = emptySet(),
        parts: Set<Retrievable> = emptySet(),
        content: List<Content> = emptyList(),
        descriptors: List<Descriptor> = emptyList()
    ) : Ingested, RetrievableWithDescriptor.Mutable, RetrievableWithContent.Mutable, RetrievableWithRelationship {

        /** Set of [Retrievable]s this [Ingested] is part of. */
        override val partOf: Set<Retrievable> = Collections.synchronizedSet(HashSet())

        /** Set of [Retrievable]s that are part of this [Ingested]. */
        override val parts: Set<Retrievable> = Collections.synchronizedSet(HashSet())

        /** List of [Content] elements associated with this [Ingested]. */
        override val content: List<Content> = Collections.synchronizedList(LinkedList())

        /** List of [Descriptor]s with this [Ingested]. */
        override val descriptors: List<Descriptor> = Collections.synchronizedList(LinkedList())

        /** */
        private val derivedContentCache: LoadingCache<DerivateName, DerivedContent> = Caffeine.newBuilder().build { name ->
            ContentDerivers[name]?.derive(this)
        }

        init {
            (this.partOf as MutableSet).addAll(partOf)
            (this.parts as MutableSet).addAll(parts)
            (this.content as MutableList).addAll(content)
            (this.descriptors as MutableList).addAll(descriptors)
        }

        /**
         * Adds a [Content] to this [Ingested.Default].
         *
         * @param content The [Content] element to add.
         */
        override fun addContent(content: Content) {
            (this.content as MutableList).add(content)
        }

        /**
         * Adds a [Descriptor] to this [Ingested.Default].
         *
         * @param descriptor The [Descriptor] element to add.
         */
        override fun addDescriptor(descriptor: Descriptor) {
            (this.descriptors as MutableList).add(descriptor)
        }

        /**
         * Generates and returns a [Content] derivative from the [Content] contained in this [RetrievableWithContent].
         *
         * @param name The [DerivateName] to use for the  [Content] derivative.
         * @return [DerivedContent] or null, if content could be derived.
         */
        override fun deriveContent(name: DerivateName): DerivedContent = this.derivedContentCache[name]

        override fun toString(): String {
            return "IngestedRetrievable.Default(id=$id, transient=$transient, partOf=$partOf, parts=$parts, content=$content, descriptors=$descriptors)"
        }

    }
}