package org.vitrivr.engine.core.model.content.element

/**
 * A textual [ContentElement].
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface TextContent: ContentElement<String> {
    /**
     * Returns the length of the [String] held by this [TextContent].
     *
     * @return Length of this [TextContent].
     */
    fun getLength(): Int = this.getContent().length
}