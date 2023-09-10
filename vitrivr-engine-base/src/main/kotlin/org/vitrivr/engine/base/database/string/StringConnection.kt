package org.vitrivr.engine.base.database.string

import org.vitrivr.engine.core.database.AbstractConnection
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.database.Persistable
import org.vitrivr.engine.base.database.string.writer.StringRetrievableWriter
import org.vitrivr.engine.core.database.retrievable.NoRetrievableInitializer


class StringConnection(internal val provider: StringConnectionProvider, schemaName: String, internal val stringify: (Persistable) -> String) : AbstractConnection(schemaName, provider) {

    override fun getRetrievableInitializer(): RetrievableInitializer = NoRetrievableInitializer()

    override fun getRetrievableWriter(): RetrievableWriter = StringRetrievableWriter(provider.targetStream, stringify)

    override fun getRetrievableReader(): RetrievableReader {
        TODO("Not yet implemented")
    }

    override fun close() {
        this.provider.targetStream.flush()
        this.provider.targetStream.close()
    }
}