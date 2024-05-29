package org.vitrivr.engine.module.features.database.string.writer

import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import java.io.OutputStream

class StringRetrievableWriter(outputStream: OutputStream, stringify: (Persistable) -> String) : StringWriter<Retrievable>(outputStream, stringify), RetrievableWriter {
    override fun connect(relationship: Relationship): Boolean {
        TODO("Not yet implemented")
    }

    override fun connectAll(relationships: Iterable<Relationship>): Boolean {
        TODO("Not yet implemented")
    }

    override fun disconnect(relationship: Relationship): Boolean {
        TODO("Not yet implemented")
    }

    override fun disconnectAll(relationships: Iterable<Relationship>): Boolean {
        TODO("Not yet implemented")
    }
}