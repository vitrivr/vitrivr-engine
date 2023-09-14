package org.vitrivr.engine.core.model.database.retrievable

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.decorators.DerivedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.derive.DerivateName

/**
 * A [Retrievable] that has [Content] elements attached to it. Typically used during ingesting and indexing.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableWithContent : Retrievable {
    /** List of [Content] elements that make-up this [Ingested]. */
    val content: List<ContentElement<*>>

    /**
     * Returns the number of [Content] elements for this [RetrievableWithContent].
     *
     * @return Number of [Content] elements for this [RetrievableWithContent].
     */
    fun contentSize(): Int = this.content.size

    /**
     * Gets the content at the provided [index].
     *
     * @param index The index of the [ContentElement] to return.
     * @return [ContentElement]
     */
    fun getContent(index: Int): ContentElement<*> = this.content[index]

    /**
     * Generates or retrieves and returns a [ContentElement] derivative from the [Content] contained in this [RetrievableWithContent].
     *
     * @param name The [DerivateName] to use for the [DerivedContent] derivative.
     * @param contentFactory The [ContentFactory] with which to generate the content
     * @return [DerivedContent] or null, if content could be derived.
     */
    fun deriveContent(name: DerivateName, contentFactory: ContentFactory): DerivedContent?

    /**
     * A [Mutable] version of the [RetrievableWithContent].
     */
    interface Mutable : RetrievableWithContent {
        /**
         * Adds a [Content] to this [RetrievableWithContent].
         *
         * @param content The [Content] element to add.
         */
        fun addContent(content: ContentElement<*>)
    }
}