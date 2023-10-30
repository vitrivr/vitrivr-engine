package org.vitrivr.engine.core.model.content.element

/**
 * A textual [ContentElement].
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface TextContent: ContentElement<String> {
    /** Length of the [String] held by this [TextContent]. */
    val length: Int
        get() = this.content.length
}