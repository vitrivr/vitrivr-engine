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

class JsonlConnection(override val schemaName: String, connectionProvider: JsonlConnectionProvider, private val root: Path) : AbstractConnection(schemaName, connectionProvider) {

    companion object {

        /** The column name of a retrievable ID. */
        const val RETRIEVABLE_ID_COLUMN_NAME = "retrievableid"

        /** The column name of a descriptor ID. */
        const val DESCRIPTOR_ID_COLUMN_NAME = "descriptorid"
    }

    override fun <T> withTransaction(action: () -> T): T {
        LOGGER.warn { "Transactions are not supported by the JsonlConnection" }
        return action()
    }

    override fun getRetrievableInitializer(): RetrievableInitializer = JsonlRetrievableInitializer(this)

    private val writer: JsonlRetrievableWriter by lazy { JsonlRetrievableWriter(this) }

    override fun getRetrievableWriter(): RetrievableWriter = writer

    private val reader: JsonlRetrievableReader by lazy { JsonlRetrievableReader(this) }

    override fun getRetrievableReader(): RetrievableReader = reader

    override fun description(): String = "JsonlConnection on '${root.absolutePathString()}'"

    override fun close() {

    }

    /**
     * The [Path] to the root directory of the JSONL connection.
     */
    internal val schemaRoot: Path = this.root.resolve(schemaName)

    /**
     * Resolves the [Path] to the file that contains the [Schema.Field] with the given name.
     *
     * @param field [Schema.Field] to resolve.
     * @return [Path] to the file that contains the [Schema.Field].
     */
    internal fun resolve(field: Schema.Field<*, *>): Path = this.schemaRoot.resolve("${field.fieldName}.jsonl")
}