package org.vitrivr.engine.core.model.retrievable.decorators

import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Retrievable

/**
 * A [Retrievable] that has [Content] elements attached to it. Typically used during ingesting and indexing.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableWithContent : Retrievable {
    /** List of [Content] elements that make-up this [Retrievable]. */
    val content: List<ContentElement<*>>

    /**
     * Returns the number of [Content] elements for this [RetrievableWithContent].
     *
     * @return Number of [Content] elements for this [RetrievableWithContent].
     */
    fun size(): Int = this.content.size

    /**
     * Gets the content at the provided [index].
     *
     * @param index The index of the [ContentElement] to return.
     * @return [ContentElement]
     */
    fun getContent(index: Int): ContentElement<*> = this.content[index]
}