package org.vitrivr.engine.core.model.content

import org.vitrivr.engine.core.source.Source
import java.awt.image.BufferedImage

data class InMemoryImageContent(override val source: Source, override val image: BufferedImage) : ImageContent {
    override val timeNs: Long? = null
    override val posX: Int? = null
    override val posY: Int? = null
    override val posZ: Int? = null
}
