package org.vitrivr.engine.database.jsonl

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.vitrivr.engine.core.database.AbstractConnection
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.jsonl.retrievable.JsonlRetrievableInitializer
import org.vitrivr.engine.database.jsonl.retrievable.JsonlRetrievableReader
import org.vitrivr.engine.database.jsonl.retrievable.JsonlRetrievableWriter
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal val LOGGER = logger("org.vitrivr.engine.database.jsonl.JsonlConnection")

class JsonlConnection(
    override val schemaName: String,
    connectionProvider: JsonlConnectionProvider,
    private val root: Path
) : AbstractConnection(schemaName, connectionProvider) {

    override fun <T> withTransaction(action: () -> T): T {
        LOGGER.warn { "Transactions are not supported by the JsonlConnection" }
        return action()
    }

    internal val schemaRoot = root.resolve(schemaName)

    fun getPath(field: Schema.Field<*, *>) = schemaRoot.resolve("${field.fieldName}.jsonl")

    override fun getRetrievableInitializer(): RetrievableInitializer = JsonlRetrievableInitializer(this)

    private val writer: JsonlRetrievableWriter by lazy { JsonlRetrievableWriter(this) }

    override fun getRetrievableWriter(): RetrievableWriter = writer

    private val reader: JsonlRetrievableReader by lazy { JsonlRetrievableReader(this) }

    override fun getRetrievableReader(): RetrievableReader = reader

    override fun description(): String = "JsonlConnection on '${root.absolutePathString()}'"

    override fun close() {
        writer.close()
    }

    companion object {

        /** The column name of a retrievable ID. */
        const val RETRIEVABLE_ID_COLUMN_NAME = "retrievableid"

        /** The column name of a descriptor ID. */
        const val DESCRIPTOR_ID_COLUMN_NAME = "descriptorid"
    }

}