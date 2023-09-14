package org.vitrivr.engine.core.model.content.element

import org.vitrivr.engine.core.model.content.Content

/**
 * A [Content] element is a piece of [Content] that is tied to some actual [Content].
 *
 * The types of [ContentElement] are restricted
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface ContentElement<T>: Content {
    /**
     * Accesses the content held by  this [ContentElement].
     *
     * @return [ContentElement]
     */
    fun getContent(): T
}