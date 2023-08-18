package org.vitrivr.engine.core.model.database.retrievable

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.derived.DerivedContent
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.operators.derive.ContentDerivers
import org.vitrivr.engine.core.operators.derive.DerivateName
import java.util.*

/**
 * A [Retrievable] used in the data ingest pipeline.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface IngestedRetrievable: Retrievable {

    /** List of [Content] elements that make-up this [IngestedRetrievable]. */
    val content: MutableList<Content>

    /** List of [Descriptor] elements that have been extracted for this [IngestedRetrievable]. */
    val descriptors: MutableList<Descriptor>

    fun getDerivedContent(name: DerivateName) : DerivedContent?

    /**
     * A [Default] implementation of an [IngestedRetrievable].
     */

    data class Default(
        override val id: UUID = UUID.randomUUID(),
        override val transient: Boolean,
        override val partOf: MutableSet<Retrievable> = mutableSetOf(),
        override val parts: MutableSet<Retrievable> = mutableSetOf(),
        override val content: MutableList<Content> = mutableListOf(),
        override val descriptors: MutableList<Descriptor> = mutableListOf()
    ): IngestedRetrievable {

        private val derivedContentCache: LoadingCache<DerivateName, DerivedContent> = Caffeine.newBuilder().build { name ->
            ContentDerivers[name]?.derive(this)
        }
        override fun getDerivedContent(name: DerivateName): DerivedContent? = derivedContentCache[name]
    }
}