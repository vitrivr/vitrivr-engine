package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import java.awt.image.BufferedImage

interface Resolver {
    fun resolve(id: RetrievableId) : Resolvable

    fun saveBufferedImage(id: RetrievableId, img: BufferedImage)

    fun saveAny(id: RetrievableId, any: Any)
}