package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.ingest.Resolvable
import org.vitrivr.engine.core.operators.ingest.Resolver
import java.awt.image.BufferedImage

class DummyResolver : Resolver {
    /***
     * Template for a [Resolver].
     *
     * @author Fynn Faber
     * @version 1.0
     */
    override fun resolve(id: RetrievableId): Resolvable {
        return object : Resolvable {
            override fun toDataUrl(): String {
                return "dummy"
            }
        }
    }

    override fun saveBufferedImage(id: RetrievableId, img: BufferedImage) {
        // do nothing
    }

    override fun saveAny(id: RetrievableId, any: Any) {
        // do nothing
    }
}