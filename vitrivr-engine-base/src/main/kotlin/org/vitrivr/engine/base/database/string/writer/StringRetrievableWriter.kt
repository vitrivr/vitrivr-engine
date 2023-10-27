package org.vitrivr.engine.base.database.string.writer

import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import java.io.OutputStream

class StringRetrievableWriter(outputStream: OutputStream, stringify: (Persistable) -> String) : StringWriter<Retrievable>(outputStream, stringify), RetrievableWriter {
    override fun connect(subject: RetrievableId, predicate: String, `object`: RetrievableId): Boolean {
        TODO("Not yet implemented")
    }

    override fun disconnect(subject: RetrievableId, predicate: String, `object`: RetrievableId): Boolean {
        TODO("Not yet implemented")
    }
}