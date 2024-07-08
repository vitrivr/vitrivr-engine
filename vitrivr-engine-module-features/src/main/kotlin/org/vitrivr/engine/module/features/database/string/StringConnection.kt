package org.vitrivr.engine.module.features.database.string

import org.vitrivr.engine.core.database.AbstractConnection
import org.vitrivr.engine.core.database.retrievable.NoRetrievableInitializer
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.module.features.database.string.writer.StringRetrievableWriter


class StringConnection(override val provider: StringConnectionProvider, schemaName: String, internal val stringify: (Persistable) -> String) : AbstractConnection(schemaName, provider) {

    override fun getRetrievableInitializer(): RetrievableInitializer = NoRetrievableInitializer()

    override fun getRetrievableWriter(): RetrievableWriter = StringRetrievableWriter(this, provider.targetStream, stringify)

    override fun getRetrievableReader(): RetrievableReader {
        TODO("Not yet implemented")
    }

    /**
     *
     */
    override fun description(): String = ""

    override fun close() {
        this.provider.targetStream.flush()
        this.provider.targetStream.close()
    }
}