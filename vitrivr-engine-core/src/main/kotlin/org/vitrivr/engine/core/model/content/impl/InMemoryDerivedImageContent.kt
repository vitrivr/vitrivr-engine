package org.vitrivr.engine.core.model.content.impl

import org.vitrivr.engine.core.model.content.DerivedContent
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.operators.derive.DerivateName
import java.awt.image.BufferedImage

/**
 * A [DerivedContent] for [ImageContent].
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
@JvmRecord
data class InMemoryDerivedImageContent(override val name: DerivateName, private val image: BufferedImage) : ImageContent, DerivedContent<BufferedImage> {
    override fun getContent(): BufferedImage = this.image
}