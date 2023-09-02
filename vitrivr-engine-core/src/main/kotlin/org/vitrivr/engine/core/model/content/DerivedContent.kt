package org.vitrivr.engine.core.model.content

import org.vitrivr.engine.core.operators.derive.DerivateName


/**
 *
 */
interface DerivedContent : Content {
    /** The name of the derivate. */
    val name: DerivateName

    /** The [Content] element this [DerivedContent] was derived from. */
    val original: Content
}