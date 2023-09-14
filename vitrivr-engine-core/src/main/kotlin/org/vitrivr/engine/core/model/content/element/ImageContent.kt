package org.vitrivr.engine.core.model.content.element

import java.awt.image.BufferedImage

/**
 * A visual (image) [ContentElement].
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface ImageContent: ContentElement<BufferedImage> {
    /**
     * Returns the width of the [BufferedImage] held by this [ContentElement].
     *
     * @return Width of the [ContentElement] in pixels.
     */
    fun getWidth() = this.getContent().width

    /**
     * Returns the height of the [BufferedImage] held by this [ContentElement].
     *
     * @return Height of the [ContentElement] in pixels.
     */
    fun getHeight() = this.getContent().width
}