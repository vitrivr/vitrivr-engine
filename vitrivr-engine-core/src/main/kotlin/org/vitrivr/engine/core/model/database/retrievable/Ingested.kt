package org.vitrivr.engine.core.model.database.retrievable

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.decorators.DerivedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.operators.derive.ContentDeriver
import org.vitrivr.engine.core.operators.derive.ContentDerivers
import org.vitrivr.engine.core.operators.derive.DerivateName
import java.util.*
import kotlin.collections.ArrayList

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
        content: List<ContentElement<*>> = emptyList(),
        descriptors: List<Descriptor> = emptyList()
    ) : Ingested, RetrievableWithDescriptor.Mutable, RetrievableWithContent.Mutable, RetrievableWithRelationship {

        /** Set of [Retrievable]s this [Ingested] is part of. */
        override val partOf: Set<Retrievable> = Collections.synchronizedSet(HashSet())

        /** Set of [Retrievable]s that are part of this [Ingested]. */
        override val parts: Set<Retrievable> = Collections.synchronizedSet(HashSet())

        /** List of [Content] elements associated with this [Ingested]. */
        override val content: List<ContentElement<*>> = Collections.synchronizedList(ArrayList())

        /** List of [Descriptor]s with this [Ingested]. */
        override val descriptors: List<Descriptor> = Collections.synchronizedList(ArrayList())

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
        override fun addContent(content: ContentElement<*>) {
            if (this.content.contains(content)) {
                return
            }
            (this.content as MutableList).add(content)
        }

        /**
         * Adds a [Descriptor] to this [Ingested.Default].
         *
         * @param descriptor The [Descriptor] element to add.
         */
        override fun addDescriptor(descriptor: Descriptor) {
            if (this.descriptors.contains(descriptor)) {
                return
            }
            (this.descriptors as MutableList).add(descriptor)
        }


        /**
         * Returns a new or existing derived content using [ContentDeriver] with specified name.
         * If a [DerivedContent] by this deriver already exists in the list of content, it is returned.
         * If a new [DerivedContent] is successfully generated, it is added to the content list.
         */
        override fun deriveContent(name: DerivateName, contentFactory: ContentFactory): DerivedContent? {

            val deriver: ContentDeriver<*> = ContentDerivers[name] ?: return null

            val existing = this.content.find { it is DerivedContent && it.deriverName == deriver.derivateName }

            if (existing != null) {
                return existing as DerivedContent
            }

            val derived = deriver.derive(this, contentFactory)

            if (derived != null && derived is ContentElement<*>) {
                addContent(derived)
            }

            return derived

        }

        override fun toString(): String {
            return "IngestedRetrievable.Default(id=$id, transient=$transient, partOf=$partOf, parts=$parts, content=$content, descriptors=$descriptors)"
        }

    }
}