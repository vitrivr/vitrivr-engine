package org.vitrivr.engine.core.model.content

import java.awt.image.BufferedImage

/**
 * A visual (image) [Content] element.
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface ImageContent: Content {
    /** The [BufferedImage] associated with this [ImageContent]. */
    val image: BufferedImage
}