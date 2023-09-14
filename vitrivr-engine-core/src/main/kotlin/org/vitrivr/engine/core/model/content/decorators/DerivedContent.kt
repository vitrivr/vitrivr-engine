package org.vitrivr.engine.core.model.content.decorators

import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.operators.derive.DerivateName


/**
 * A [Content] element that was not originally extracted but derived.
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface DerivedContent : ContentDecorator {
    /** The name of the derivative. */
    val deriverName: DerivateName
}