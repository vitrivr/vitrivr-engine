package org.vitrivr.engine.core.model.content.impl

import org.vitrivr.engine.core.model.content.ImageContent
import java.awt.image.BufferedImage

/**
 * A naive in-memory implementation of the [ImageContent] interface.
 *
 * Warning: Usage of [InMemoryImageContent] may lead to out-of-memory situations in large extraction pipelines.
 *
 * @author Luca Rossetto.
 * @version 1.0.0
 */
@JvmRecord
data class InMemoryImageContent(private val image: BufferedImage): ImageContent {
    override fun getContent(): BufferedImage = this.image
}
