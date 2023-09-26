package org.vitrivr.engine.core.model.content

import org.vitrivr.engine.core.model.content.decorators.ContentDecorator
import org.vitrivr.engine.core.model.content.element.ContentElement

/**
 * [Content] as handled by vitrivr.
 *
 * This is the common root of all [Content]s handled by vitrivr. Typically, a [Content]
 * implementation implements a single [ContentElement] and zero to many [ContentDecorator]s.
 *
 * @see ContentElement
 * @see ContentDecorator
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Content