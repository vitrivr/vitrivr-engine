package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.source.file.MimeType
import java.awt.image.BufferedImage

interface Resolver {
    fun resolve(id: RetrievableId) : Resolvable?

    fun getOutputStream(id: RetrievableId, mimeType: MimeType) : java.io.OutputStream
}