package org.vitrivr.engine.core.model.database.retrievable

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.DerivedContent
import org.vitrivr.engine.core.operators.derive.DerivateName

/**
 * A [Retrievable] that has [Content] elements attached to it. Typically used during ingesting and indexing.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableWithContent : Retrievable {
    /** List of [Content] elements that make-up this [Ingested]. */
    val content: List<Content<*>>

    /**
     * Returns the number of [Content] elements for this [RetrievableWithContent].
     *
     * @return Number of [Content] elements for this [RetrievableWithContent].
     */
    fun contentSize(): Int = this.content.size

    /**
     * Gets the content at the provided [index].
     *
     * @param index The index of the [Content] to return.
     * @return [Content]
     */
    fun getContent(index: Int): Content<*> = this.content[index]

    /**
     * Generates or retrieves and returns a [Content] derivative from the [Content] contained in this [RetrievableWithContent].
     *
     * @param name The [DerivateName] to use for the  [Content] derivative.
     * @param contentFactory The [ContentFactory] with which to generate the content
     * @return [DerivedContent] or null, if content could be derived.
     */
    fun deriveContent(name: DerivateName, contentFactory: ContentFactory): DerivedContent<*>?

    /**
     * A [Mutable] version of the [RetrievableWithContent].
     */
    interface Mutable : RetrievableWithContent {
        /**
         * Adds a [Content] to this [RetrievableWithContent].
         *
         * @param content The [Content] element to add.
         */
        fun addContent(content: Content<*>)
    }
}