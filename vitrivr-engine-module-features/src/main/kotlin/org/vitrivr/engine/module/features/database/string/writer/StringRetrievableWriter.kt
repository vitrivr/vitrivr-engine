package org.vitrivr.engine.module.features.database.string.writer

import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.module.features.database.string.StringConnection
import java.io.OutputStream

class StringRetrievableWriter(connection: StringConnection, outputStream: OutputStream, stringify: (Persistable) -> String) : StringWriter<Retrievable>(connection, outputStream, stringify), RetrievableWriter {
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