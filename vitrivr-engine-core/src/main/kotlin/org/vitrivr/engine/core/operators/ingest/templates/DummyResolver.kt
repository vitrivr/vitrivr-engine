package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.resolver.Resolvable
import org.vitrivr.engine.core.operators.resolver.Resolver
import org.vitrivr.engine.core.source.file.MimeType
import java.io.OutputStream

/***
 * Template for a [Resolver].
 *
 * @author Fynn Faber
 * @version 1.0
 */
class DummyResolver : Resolver {
    override fun resolve(id: RetrievableId): Resolvable? {
        return null
    }
}