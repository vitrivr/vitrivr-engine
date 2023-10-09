package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.ingest.Resolvable
import org.vitrivr.engine.core.operators.ingest.Resolver
import org.vitrivr.engine.core.source.file.MimeType
import java.io.OutputStream

class DummyResolver : Resolver {
    /***
     * Template for a [Resolver].
     *
     * @author Fynn Faber
     * @version 1.0
     */
    override fun resolve(id: RetrievableId): Resolvable? {
        return null
    }

    override fun getOutputStream(id: RetrievableId, mimeType: MimeType): OutputStream {
        return OutputStream.nullOutputStream()
    }


}