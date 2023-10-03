package org.vitrivr.engine.base.resolvers

import org.vitrivr.engine.core.operators.ingest.Resolvable
import org.vitrivr.engine.core.util.extension.toDataURL
import java.awt.image.BufferedImage

class ResolvableImage(val image: BufferedImage) : Resolvable {
    override fun toDataUrl(): String {
        return image.toDataURL()
    }
}