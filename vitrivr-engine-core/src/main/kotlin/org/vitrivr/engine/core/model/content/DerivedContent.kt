package org.vitrivr.engine.core.model.content

import org.vitrivr.engine.core.operators.derive.DerivateName


/**
 * A [Content] element that was not originally extracted but derived.
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface DerivedContent : Content {
    /** The name of the derivate. */
    val name: DerivateName

    /** The [Content] element this [DerivedContent] was derived from. */
    val original: Content
}