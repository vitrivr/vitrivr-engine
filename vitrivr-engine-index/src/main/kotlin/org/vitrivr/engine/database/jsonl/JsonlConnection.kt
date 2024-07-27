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
import java.io.File

internal val LOGGER = logger("org.vitrivr.engine.database.jsonl.JsonlConnection")

class JsonlConnection(
    override val schemaName: String,
    connectionProvider: JsonlConnectionProvider,
    private val root: File
) : AbstractConnection(schemaName, connectionProvider) {

    override fun <T> withTransaction(action: (Unit) -> T): T {
        LOGGER.warn { "Transactions are not supported by the JsonlConnection" }
        return action(Unit)
    }

    private val schemaRoot = File(root, schemaName)

    fun getFile(field: Schema.Field<*, *>) = File(schemaRoot, "${field.fieldName}.jsonl")

    override fun getRetrievableInitializer(): RetrievableInitializer = JsonlRetrievableInitializer(this)

    private val writer: JsonlRetrievableWriter by lazy { JsonlRetrievableWriter(this) }

    override fun getRetrievableWriter(): RetrievableWriter = writer

    private val reader: JsonlRetrievableReader by lazy { JsonlRetrievableReader(this) }

    override fun getRetrievableReader(): RetrievableReader = reader

    override fun description(): String = "JsonlConnection on '${root.absolutePath}'"

    override fun close() {
        writer.close()
        reader.close()
    }

    companion object {

        /** The name of the retrievable entity. */
        const val RETRIEVABLE_ENTITY_NAME = "retrievable"

        /** The column name of a retrievable ID. */
        const val RETRIEVABLE_ID_COLUMN_NAME = "retrievableId"

        /** The column name of a retrievable ID. */
        const val RETRIEVABLE_TYPE_COLUMN_NAME = "type"

        /** The name of the retrievable entity. */
        const val RELATIONSHIP_ENTITY_NAME = "relationships"

        /** The column name of a retrievable ID. */
        const val SUBJECT_ID_COLUMN_NAME = "subjectId"

        /** The column name of a retrievable ID. */
        const val OBJECT_ID_COLUMN_NAME = "objectId"

        /** The column name of a retrievable ID. */
        const val PREDICATE_COLUMN_NAME = "predicate"

        /** The prefix for descriptor entities. */
        const val DESCRIPTOR_ENTITY_PREFIX = "descriptor"

        /** The column name of a descriptor ID. */
        const val DESCRIPTOR_ID_COLUMN_NAME = "descriptorId"
    }

}